package boss;

import boss.BossManager.*;
import consts.AppearType;
import consts.BossStatus;
import consts.BossType;
import consts.ConstPlayer;
import services.map.ChangeMapService;
import services.map.MapService;
import map.Zone;
import mob.Mob;
import network.Message;
import network.inetwork.IBoss;
import player.Pet;
import player.Player;
import services.PlayerService;
import server.ServerNotify;
import services.Service;
import services.SkillService;
import services.TaskService;
import player.skill.Skill;
import logger.MyLogger;
import utils.SkillUtil;
import utils.Util;

import java.io.IOException;
import java.util.List;

public class Boss extends Player implements IBoss {

    private static final int DEFAULT_MP_G = 31_07_2002;
    private static final short MONKEY_BODY = 193;
    private static final short MONKEY_LEG = 194;
    private static final int MAX_CHAT_DISTANCE = 600;
    private static final int MAX_TIME_CHAT_MS = 2000;
    private static final int TIME_DELAY_ATTACK_MS = 100;
    private static final int DEFAULT_ATTACK_RANGE = 500;
    private static final int BOM_TIME_MS = 2500;
    private static final int MAX_ERROR_COUNT = 5;

    public final BossData[] data;
    public int currentLevel = -1;
    public BossStatus bossStatus;
    public Boss[][] bossAppearTogether;
    public Zone zoneFinal = null;
    public Player playerReward;
    public int lv;
    public int error;
    public boolean prepareBom;
    public boolean isNotifyDisabled;
    public boolean isZone01SpawnDisabled;

    protected Zone lastZone;
    protected long lastTimeRest;
    protected int secondsRest;
    protected long lastTimeChatS;
    protected int timeChatS;
    protected byte indexChatS;
    protected long lastTimeChatE;
    protected int timeChatE;
    protected byte indexChatE;
    protected long lastTimeChatM;
    protected int timeChatM;
    protected long lastTimeTargetPlayer;
    protected int timeTargetPlayer;
    protected Player playerTarger;
    protected Boss parentBoss;
    protected long lastTimeAttack;

    public Boss(int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(BossType bossType, int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(bossType, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }

        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        BossManager.gI().addBoss(this);
        initBossAppearTogether();
    }

    public Boss(BossType bossType, int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }

        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        assignToManager(bossType);
        initBossAppearTogether();
    }

    private void assignToManager(BossType bossType) {
        switch (bossType) {
            case YARDART ->
                YardartManager.gI().addBoss(this);
            case FINAL ->
                FinalBossManager.gI().addBoss(this);
            case SKILLSUMMONED ->
                SkillSummonedManager.gI().addBoss(this);
            case BROLY ->
                BrolyManager.gI().addBoss(this);
            case PHOBAN ->
                OtherBossManager.gI().addBoss(this);
            case PHOBANDT ->
                RedRibbonHQManager.gI().addBoss(this);
            case PHOBANBDKB ->
                TreasureUnderSeaManager.gI().addBoss(this);
            case PHOBANCDRD ->
                SnakeWayManager.gI().addBoss(this);
            case PHOBANKGHD ->
                GasDestroyManager.gI().addBoss(this);
            case TRUNGTHU_EVENT ->
                TrungThuEventManager.gI().addBoss(this);
            case HALLOWEEN_EVENT ->
                HalloweenEventManager.gI().addBoss(this);
            case CHRISTMAS_EVENT ->
                ChristmasEventManager.gI().addBoss(this);
            case HUNGVUONG_EVENT ->
                HungVuongEventManager.gI().addBoss(this);
            case TET_EVENT ->
                LunarNewYearEventManager.gI().addBoss(this);
        }
    }

    private void initBossAppearTogether() throws Exception {
        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            int[] subBossIds = this.data[i].getBossesAppearTogether();
            if (subBossIds == null) {
                continue;
            }

            this.bossAppearTogether[i] = new Boss[subBossIds.length];
            for (int j = 0; j < subBossIds.length; j++) {
                Boss boss = BossManager.gI().createBoss(subBossIds[j]);
                if (boss != null) {
                    boss.parentBoss = this;
                    boss.lv = j;
                    this.bossAppearTogether[i][j] = boss;
                }
            }
        }
    }

    @Override
    public void initBase() {
        BossData currentData = this.data[this.currentLevel];
        this.name = String.format(currentData.getName(), Util.nextInt(0, 100));
        this.gender = currentData.getGender();
        this.nPoint.mpg = DEFAULT_MP_G;
        this.nPoint.dameg = currentData.getDame();
        this.nPoint.hpg = currentData.getHp()[Util.nextInt(0, currentData.getHp().length - 1)];
        this.nPoint.hp = nPoint.hpg;
        this.nPoint.calPoint();
        this.initSkill();
        this.resetBase();
    }

    protected void initSkill() {
        for (Skill skill : this.playerSkill.skills) {
            skill.dispose();
        }
        this.playerSkill.skills.clear();
        this.playerSkill.skillSelect = null;

        int[][] skillTemps = data[this.currentLevel].getSkillTemp();
        for (int[] skillTemp : skillTemps) {
            Skill skill = SkillUtil.createSkill(skillTemp[0], skillTemp[1]);
            if (skillTemp.length == 3) {
                skill.coolDown = skillTemp[2];
            }
            this.playerSkill.skills.add(skill);
        }
    }

    protected void resetBase() {
        this.lastTimeChatS = 0;
        this.lastTimeChatE = 0;
        this.timeChatS = 0;
        this.timeChatE = 0;
        this.indexChatS = 0;
        this.indexChatE = 0;
    }

    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        return this.data[this.currentLevel].getOutfit()[0];
    }

    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return MONKEY_BODY;
        }
        return this.data[this.currentLevel].getOutfit()[1];
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return MONKEY_LEG;
        }
        return this.data[this.currentLevel].getOutfit()[2];
    }

    @Override
    public short getFlagBag() {
        return this.data[this.currentLevel].getOutfit()[3];
    }

    @Override
    public byte getAura() {
        return (byte) this.data[this.currentLevel].getOutfit()[4];
    }

    @Override
    public byte getEffFront() {
        return (byte) this.data[this.currentLevel].getOutfit()[5];
    }

    public Zone getMapJoin() {
        int[] maps = this.data[this.currentLevel].getMapJoin();
        int mapId = maps[Util.nextInt(0, maps.length - 1)];
        return MapService.gI().getMapWithRandZone(mapId);
    }

    @Override
    public void changeStatus(BossStatus status) {
        this.bossStatus = status;
    }

    @Override
    public Player getPlayerAttack() {
        if (this.zone == null) {
            return null;
        }

        if (this.playerTarger != null && (this.playerTarger.isDie() || !this.zone.equals(this.playerTarger.zone))) {
            this.playerTarger = null;
        }

        if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            this.playerTarger = this.zone.getRandomPlayerInMap();
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(5000, 7000);
        }

        if (this.playerTarger != null && this.playerTarger.isPet) {
            Pet pet = (Pet) this.playerTarger;
            if (pet.master != null && pet.master.equals(this)) {
                this.playerTarger = null;
            }
        }
        return this.playerTarger;
    }

    @Override
    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }

    @Override
    public void changeToTypeNonPK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
    }

    @Override
    public void updateInfo() {
        super.update();
    }

    @Override
    public void update() {
        if (prepareBom) {
            return;
        }

        super.update();
        this.nPoint.mp = this.nPoint.mpg;

        if (this.effectSkill == null || this.effectSkill.isHaveEffectSkill() || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
            return;
        }

        switch (this.bossStatus) {
            case CHAT_S, AFK, ACTIVE ->
                this.autoLeaveMap();
        }

        switch (this.bossStatus) {
            case REST ->
                this.rest();
            case RESPAWN -> {
                this.respawn();
                this.changeStatus(BossStatus.JOIN_MAP);
            }
            case JOIN_MAP ->
                this.joinMap();
            case CHAT_S ->
                handleChatSState();
            case AFK ->
                this.afk();
            case ACTIVE ->
                handleActiveState();
            case DIE ->
                this.changeStatus(BossStatus.CHAT_E);
            case CHAT_E ->
                handleChatEState();
            case LEAVE_MAP ->
                this.leaveMap();
        }
    }

    private void handleChatSState() {
        if (chatS()) {
            this.doneChatS();
            this.lastTimeChatM = System.currentTimeMillis();
            this.timeChatM = 5000;
            if (this.bossStatus != BossStatus.AFK) {
                this.changeStatus(BossStatus.ACTIVE);
            }
        }
    }

    private void handleActiveState() {
        this.chatM();
        if ((this.effectSkill.isCharging && !Util.isTrue(1, 20)) || this.effectSkill.useTroi) {
            return;
        }
        this.active();
    }

    private void handleChatEState() {
        if (chatE()) {
            this.doneChatE();
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    @Override
    public void rest() {
        int nextLevel = (this.currentLevel + 1 >= this.data.length) ? 0 : this.currentLevel + 1;
        if (this.data[nextLevel].getTypeAppear() == AppearType.DEFAULT_APPEAR && Util.canDoWithTime(lastTimeRest, secondsRest * 1000L)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void afk() {
    }

    @Override
    public void respawn() {
        this.currentLevel++;
        if (this.currentLevel >= this.data.length) {
            this.currentLevel = 0;
        }
        this.initBase();
        this.changeToTypeNonPK();
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.wakeupAnotherBossWhenAppear();
            return;
        }

        this.zone = determineSpawnZone();
        if (this.zone == null) {
            this.changeStatus(BossStatus.RESPAWN);
            return;
        }

        try {
            if (this.currentLevel == 0) {
                spawnLevelZero();
            } else {
                ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
            }
            Service.gI().sendFlagBag(this);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
        } catch (Exception e) {
            handleJoinMapError(e);
        }
    }

    private Zone determineSpawnZone() {
        if (this.zone != null) {
            return this.zone;
        }
        if (this.parentBoss != null) {
            return this.parentBoss.zone;
        }
        if (this.lastZone != null) {
            return this.lastZone;
        }
        return getMapJoin();
    }

    private void spawnLevelZero() {
        if (this.parentBoss == null) {
            Zone validZone = findValidZoneToSpawn();
            if (validZone == null) {
                this.changeStatus(BossStatus.REST);
                this.zone = null;
                this.lastZone = null;
                return;
            }
            this.zone = validZone;
            int x = Util.nextInt(100, Math.max(100, this.zone.map.mapWidth - 100));
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
        } else {
            int x = this.parentBoss.location.x - (this.lv + 1) * 30;
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
        }
        this.wakeupAnotherBossWhenAppear();
    }

    private Zone findValidZoneToSpawn() {
        int zoneIdToSpawn = 0;
        if (this.isZone01SpawnDisabled && this.zone.map.zones.size() > 2) {
            zoneIdToSpawn = Util.nextInt(2, this.zone.map.zones.size() - 1);
            while (zoneIdToSpawn < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneIdToSpawn).getBosses().isEmpty()) {
                zoneIdToSpawn++;
            }
            return (zoneIdToSpawn < this.zone.map.zones.size()) ? this.zone.map.zones.get(zoneIdToSpawn) : null;
        } else {
            while (zoneIdToSpawn < this.zone.map.zones.size() && this.zone.map.zones.get(zoneIdToSpawn).getNumOfPlayers() > 10) {
                zoneIdToSpawn++;
            }
            while (zoneIdToSpawn < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneIdToSpawn).getBosses().isEmpty()) {
                zoneIdToSpawn++;
            }
            return (zoneIdToSpawn < this.zone.map.zones.size()) ? this.zone.map.zones.get(zoneIdToSpawn) : this.zone.map.zones.get(0);
        }
    }

    private void handleJoinMapError(Exception e) {
        this.changeStatus(BossStatus.REST);
        if (error < MAX_ERROR_COUNT) {
            MyLogger.logWarning("Lỗi Boss joinMap: " + e);
            error++;
        }
    }

    public void joinMapByZone(Zone zone) {
        if (zone == null) {
            return;
        }
        this.zone = zone;
        int x = Util.nextInt(100, Math.max(100, this.zone.map.mapWidth - 100));
        int y = this.zone.map.yPhysicInTop(x, 100);
        ChangeMapService.gI().changeMap(this, this.zone, x, y);
    }

    protected void notifyJoinMap() {
        if (canSendNotify()) {
            ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }

    private boolean canSendNotify() {
        if (this.isNotifyDisabled || this.zone == null || this.zone.map == null) {
            return false;
        }
        int mapId = this.zone.map.mapId;
        return !(mapId == 140 || mapId == 111
                || MapService.gI().isMapPhoBan(mapId)
                || MapService.gI().isMapMaBu(mapId)
                || MapService.gI().isMapBlackBallWar(mapId));
    }

    @Override
    public boolean chatS() {
        if (!Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            return false;
        }

        String[] textS = this.data[this.currentLevel].getTextS();
        if (this.indexChatS >= textS.length) {
            return true;
        }

        String textChat = textS[this.indexChatS];
        int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);

        if (!this.chat(prefix, textChat)) {
            return false;
        }

        this.lastTimeChatS = System.currentTimeMillis();
        this.timeChatS = Math.min(textChat.length() * 100, MAX_TIME_CHAT_MS);
        this.indexChatS++;

        return false;
    }

    @Override
    public void doneChatS() {
    }

    @Override
    public void chatM() {
        if (this.typePk == ConstPlayer.NON_PK || !Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }

        String[] textM = this.data[this.currentLevel].getTextM();
        if (textM == null || textM.length == 0) {
            return;
        }

        String textChat = textM[Util.nextInt(0, textM.length - 1)];
        int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);

        this.chat(prefix, textChat);
        this.lastTimeChatM = System.currentTimeMillis();
        this.timeChatM = Util.nextInt(3000, 20000);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public void attack() {
        if (!Util.canDoWithTime(this.lastTimeAttack, TIME_DELAY_ATTACK_MS) || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }

        this.lastTimeAttack = System.currentTimeMillis();
        try {
            Player target = getPlayerAttack();
            if (target == null || target.isDie()) {
                return;
            }

            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));

            if (Util.getDistance(this, target) <= this.getRangeCanAttackWithSkillSelect()) {
                if (Util.isTrue(5, 20)) {
                    int offsetX = Util.getOne(-1, 1) * (SkillUtil.isUseSkillChuong(this) ? Util.nextInt(20, 200) : Util.nextInt(10, 40));
                    int offsetY = Util.nextInt(10) % 2 == 0 ? 0 : -Util.nextInt(0, SkillUtil.isUseSkillChuong(this) ? 70 : 50);
                    this.moveTo(target.location.x + offsetX, target.location.y + offsetY);
                }
                SkillService.gI().useSkill(this, target, null, -1, null);
                checkPlayerDie(target);
            } else {
                if (Util.isTrue(1, 2)) {
                    this.moveToPlayer(target);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void checkPlayerDie(Player player) {
        if (player.isDie()) {
        }
    }

    protected int getRangeCanAttackWithSkillSelect() {
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        }
        if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK || skillId == Skill.LIEN_HOAN || skillId == Skill.KAIOKEN) {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return DEFAULT_ATTACK_RANGE;
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
            MyLogger.logInformation(plKill.name + ": đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public boolean chatE() {
        if (!Util.canDoWithTime(lastTimeChatE, timeChatE)) {
            return false;
        }

        String[] textE = this.data[this.currentLevel].getTextE();
        if (this.indexChatE >= textE.length) {
            return true;
        }

        String textChat = textE[this.indexChatE];
        int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);

        if (!this.chat(prefix, textChat)) {
            return false;
        }

        this.lastTimeChatE = System.currentTimeMillis();
        this.timeChatE = Math.min(textChat.length() * 100, MAX_TIME_CHAT_MS);
        this.indexChatE++;

        return false;
    }

    @Override
    public void doneChatE() {
    }

    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            this.changeStatus(BossStatus.RESPAWN);
        } else {
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
        }
        this.wakeupAnotherBossWhenDisappear();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }

        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }

        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
        }

        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public void moveToPlayer(Player pl) {
        if (pl.location != null) {
            moveTo(pl.location.x, pl.location.y);
        }
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.zone == null || this.zone.map == null) {
            return;
        }

        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        int move = Util.nextInt(40, 60);
        int newX = this.location.x + (dir == 1 ? move : -move);
        int groundY = this.zone.map.yPhysicInTop(newX, this.location.y);

        int newY = Math.min(y, groundY);
        PlayerService.gI().playerMove(this, newX, newY);
    }

    public void chat(String text) {
        Service.gI().chat(this, text);
    }

    protected boolean chat(int prefix, String textChat) {
        switch (prefix) {
            case -1 ->
                this.chat(textChat);
            case -2 -> {
                if (this.zone == null) {
                    return false;
                }
                Player plMap = this.zone.getRandomPlayerInMap();
                if (plMap != null && !plMap.isDie() && Util.getDistance(this, plMap) <= MAX_CHAT_DISTANCE) {
                    Service.gI().chat(plMap, textChat);
                } else {
                    return false;
                }
            }
            case -3 -> {
                if (this.parentBoss != null && !this.parentBoss.isDie()) {
                    this.parentBoss.chat(textChat);
                }
            }
            default -> {
                if (prefix >= 0) {
                    Boss targetBoss = getSubBoss(prefix);
                    if (targetBoss != null && !targetBoss.isDie()) {
                        targetBoss.chat(textChat);
                    }
                }
            }
        }
        return true;
    }

    private Boss getSubBoss(int prefix) {
        if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
            return this.bossAppearTogether[this.currentLevel][prefix];
        }
        if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
            return this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][prefix];
        }
        return null;
    }

    @Override
    public void wakeupAnotherBossWhenAppear() {
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }

        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            int nextLevelBoss = (boss.currentLevel + 1 >= boss.data.length) ? 0 : boss.currentLevel + 1;
            AppearType appearType = boss.data[nextLevelBoss].getTypeAppear();

            if (appearType == AppearType.CALL_BY_ANOTHER || appearType == AppearType.APPEAR_WITH_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
            }
            if (appearType == AppearType.APPEAR_WITH_ANOTHER) {
                boss.changeStatus(BossStatus.RESPAWN);
            }
        }
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
    }

    @Override
    public void autoLeaveMap() {
    }

    public void leaveMapNew() {
        if (this.data != null) {
            this.currentLevel = this.data.length;
        }
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    @Override
    public void setBom(Player plAtt) {
        try {
            if (prepareBom) {
                return;
            }

            prepareBom = true;
            this.nPoint.hp = 1;
            long lastTime = System.currentTimeMillis();
            Service.gI().chat(this, "Rồi, rồi, mày xong rồi!");
            sendBomAnimation();

            while (prepareBom) {
                if (Util.canDoWithTime(lastTime, BOM_TIME_MS)) {
                    executeBomDamage(plAtt);
                    prepareBom = false;
                }
            }
        } catch (Exception e) {
            if (prepareBom) {
                prepareBom = false;
            }
            setDie(this);
            die(plAtt);
        }
    }

    private void sendBomAnimation() {
        try {
            Message msg = new Message(-45);
            msg.writer().writeByte(7);
            msg.writer().writeInt((int) this.id);
            msg.writer().writeShort(104);
            msg.writer().writeShort(2000);
            Service.gI().sendMessAllPlayerInMap(this, msg);
            msg.cleanup();
        } catch (IOException ignored) {
        }
    }

    private void executeBomDamage(Player plAtt) {
        setDie(this);
        die(plAtt);
        long damage = this.nPoint.hpMax;

        if (this.zone == null) {
            return;
        }

        for (Mob mob : this.zone.mobs) {
            mob.injured(this, damage, true);
        }

        if (!MapService.gI().isMapOffline(this.zone.map.mapId)) {
            List<Player> playersMap = this.zone.getNotBosses();
            for (int i = playersMap.size() - 1; i >= 0; i--) {
                Player pl = playersMap.get(i);
                if (!this.equals(pl)) {
                    pl.injured(this, damage, false, false);
                    PlayerService.gI().sendInfoHpMpMoney(pl);
                    Service.gI().Send_Info_NV(pl);
                }
            }
        }
    }
}
