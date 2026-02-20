package map;

import boss.Boss;
import boss.BossID;
import boss.Training.TrainingBoss;
import consts.ConstMob;
import consts.ConstTask;
import consts.ConstMap;
import item.Item;
import lombok.Getter;
import lombok.Setter;
import services.map.ItemMapService;
import services.map.MapService;
import services.map.NpcManager;
import mob.Mob;
import network.Message;
import npc.NonInteractiveNPC;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.PlayerService;
import services.ItemService;
import services.Service;
import services.TaskService;
import utils.FileIO;
import logger.MyLogger;
import utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Zone {

    public static final byte PLAYERS_TIEU_CHUAN_TRONG_MAP = 7;

    private static final int ITEM_APPLE = 74;
    private static final int ITEM_BABY = 78;
    private static final int ITEM_BLACK_BALL_DROP = 726;
    private static final int ITEM_CHRISTMAS_BOX = 648;
    private static final int ITEM_CHRISTMAS_SOCK = 649;
    private static final int ITEM_NO_NAME = 73;
    private static final int ITEM_TYPE_CANT_PICK = 22;

    private static final byte CMD_PICK_ITEM = -20;
    private static final byte CMD_INFO_PLAYER = -5;
    private static final byte CMD_PLAYER_DIE = -8;
    private static final byte CMD_MAP_INFO = -24;

    private static final int MOB_HIRUDEGARN_PART = 70;
    private static final int MAX_MAP_170 = 170;

    public final List<Mob> mobs;
    public final List<ItemMap> items;
    
    @Getter private final List<Player> nonInteractiveNPCs;
    @Getter private final List<Player> humanoids;
    @Getter private final List<Player> notBosses;
    @Getter private final List<Player> players;
    @Getter private final List<Player> bosses;
    private final List<Player> pets;
    
    public int countItemAppeaerd = 0;
    public Map map;
    public int zoneId;
    public int maxPlayer;
    public int shenronType = -1;
    public long lastTimeDropBlackBall;
    public boolean finishBlackBallWar;
    public boolean finishMapMaBu;

    public boolean isbulon1Alive = true;
    public boolean isbulon2Alive = true;
    public boolean isTUTAlive = true;
    public boolean isGoldenFriezaAlive;

    public boolean isCompeting;
    public String rankName1;
    public String rankName2;
    public int rank1;
    public int rank2;

    public List<TrapMap> trapMaps;
    public List<MaBuHold> maBuHolds;

    @Setter @Getter
    public Player Npc;

    public Zone(Map map, int zoneId, int maxPlayer) {
        this.map = map;
        this.zoneId = zoneId;
        this.maxPlayer = maxPlayer;
        this.nonInteractiveNPCs = new ArrayList<>();
        this.humanoids = new ArrayList<>();
        this.notBosses = new ArrayList<>();
        this.players = new ArrayList<>();
        this.bosses = new ArrayList<>();
        this.pets = new ArrayList<>();
        this.mobs = new ArrayList<>();
        this.items = new ArrayList<>();
        this.trapMaps = new ArrayList<>();
        this.maBuHolds = new ArrayList<>();
    }

    public boolean isFullPlayer() {
        return this.players.size() >= this.maxPlayer;
    }

    public void update() {
        udMob();
        udItem();
        udPlayer();
        udNonInteractiveNPC();
    }

    private void udMob() {
        for (int i = this.mobs.size() - 1; i >= 0; i--) {
            try {
                mobs.get(i).update();
            } catch (Exception e) {
                MyLogger.logError(e, "Lỗi update mobs");
            }
        }
    }

    private void udNonInteractiveNPC() {
        if (this.nonInteractiveNPCs.isEmpty()) return;
        
        for (int i = this.nonInteractiveNPCs.size() - 1; i >= 0; i--) {
            try {
                Player pl = this.nonInteractiveNPCs.get(i);
                if (pl != null && pl.zone != null) {
                    pl.update();
                }
            } catch (Exception e) {
                MyLogger.logError(e, "Lỗi update npcs");
            }
        }
    }

    private void udItem() {
        if (this.items.isEmpty()) return;
        
        for (int i = this.items.size() - 1; i >= 0; i--) {
            try {
                ItemMap item = this.items.get(i);
                if (item != null && item.itemTemplate != null) {
                    item.update();
                } else {
                    items.remove(i);
                    System.err.println("Remove item " + i);
                }
            } catch (Exception e) {
                MyLogger.logError(e, "Lỗi item");
            }
        }
    }

    private void udPlayer() {
        for (int i = this.notBosses.size() - 1; i >= 0; i--) {
            Player pl = this.notBosses.get(i);
            if (!pl.isPet && !pl.isNewPet) {
                pl.update();
            }
        }
    }

    public int getNumOfPlayers() {
        return this.players.size();
    }

    public int getNumOfBosses() {
        return this.bosses.size();
    }

    public boolean isBossCanJoin(Boss boss) {
        for (Player b : this.bosses) {
            if (b.id == boss.id) return false;
        }
        return true;
    }

    public void addPlayer(Player player) {
        if (player == null) return;
        
        if (!this.humanoids.contains(player)) this.humanoids.add(player);
        if (player instanceof NonInteractiveNPC) this.nonInteractiveNPCs.add(player);
        if (!player.isBoss && !this.notBosses.contains(player) && !player.isNewPet && !(player instanceof NonInteractiveNPC)) {
            this.notBosses.add(player);
        }
        if (!player.isBoss && !player.isNewPet && !player.isPet && !this.players.contains(player) && !(player instanceof NonInteractiveNPC)) {
            this.players.add(player);
        }
        if (player.isBoss) this.bosses.add(player);
        if (player.isPet || player.isNewPet) this.pets.add(player);
    }

    public void removePlayer(Player player) {
        this.nonInteractiveNPCs.remove(player);
        this.humanoids.remove(player);
        this.notBosses.remove(player);
        this.players.remove(player);
        this.bosses.remove(player);
        this.pets.remove(player);
    }

    public ItemMap getItemMapByItemMapId(int itemId) {
        for (ItemMap item : this.items) {
            if (item != null && item.itemMapId == itemId) return item;
        }
        return null;
    }

    public ItemMap getItemMapByTempId(int tempId) {
        for (ItemMap item : this.items) {
            if (item.itemTemplate.id == tempId) return item;
        }
        return null;
    }

    public List<ItemMap> getItemMapsForPlayer(Player player) {
        List<ItemMap> list = new ArrayList<>();
        for (ItemMap item : items) {
            if (item.itemTemplate.id == ITEM_BABY && TaskService.gI().getIdTask(player) != ConstTask.TASK_3_1) continue;
            if (item.itemTemplate.id == ITEM_APPLE && TaskService.gI().getIdTask(player) < ConstTask.TASK_3_0) continue;
            if (item.itemTemplate.id == ITEM_BLACK_BALL_DROP && item.playerId != player.id) continue;
            
            list.add(item);
        }
        return list;
    }

    public Player getPlayerInMap(long idPlayer) {
        for (Player pl : humanoids) {
            if (pl != null && pl.id == idPlayer) return pl;
        }
        return null;
    }

    public Player getPlayerInMapOffline(Player player, long idPlayer) {
        for (Player pl : bosses) {
            if (pl.id == idPlayer && pl instanceof TrainingBoss && ((TrainingBoss) pl).playerAtt.equals(player)) {
                return pl;
            }
        }
        return null;
    }

    public void pickItem(Player player, int itemMapId) {
        ItemMap itemMap = getItemMapByItemMapId(itemMapId);
        if (itemMap == null || itemMap.isPickedUp || itemMap.itemTemplate == null) return;

        synchronized (itemMap) {
            if (itemMap.isPickedUp) return;
            if (itemMap.itemTemplate.type == ITEM_TYPE_CANT_PICK) return;

            int playerIdOwner = Math.abs(itemMap.playerId > 100_000_000 ? 1_000_000_000 - (int) itemMap.playerId : (int) itemMap.playerId);
            if (playerIdOwner != player.id && itemMap.playerId != player.id && itemMap.playerId != -1) {
                Service.gI().sendThongBao(player, "Không thể nhặt vật phẩm của người khác");
                return;
            }

            Item item = ItemService.gI().createItemFromItemMap(itemMap);

            if (item.template.id == ITEM_CHRISTMAS_BOX && !InventoryService.gI().findItemTatVoGiangSinh(player)) {
                Service.gI().sendThongBao(player, "Cần thêm Tất,vớ giáng sinh");
                return;
            }

            if (!canPickUpItem(player, item)) {
                Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống, không thể nhặt thêm");
                return;
            }

            processPickItemSuccess(player, itemMap, item);
        }
    }

    private boolean canPickUpItem(Player player, Item item) {
        if (InventoryService.gI().addItemBag(player, item)) return true;
        return ItemMapService.gI().isBlackBall(item.template.id)
                || ItemMapService.gI().isNamecBall(item.template.id)
                || ItemMapService.gI().isNamecBallStone(item.template.id);
    }

    private void processPickItemSuccess(Player player, ItemMap itemMap, Item item) {
        try {
            Message msg = new Message(CMD_PICK_ITEM);
            msg.writer().writeShort(itemMap.itemMapId);
            sendItemPickMessage(msg, item, player);
            player.sendMessage(msg);
            msg.cleanup();

            Service.gI().sendToAntherMePickItem(player, itemMap.itemMapId);

            if (itemMap.itemTemplate.id != ITEM_APPLE) {
                itemMap.isPickedUp = true;
            }

            if (item.template.id == ITEM_CHRISTMAS_BOX) {
                InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, ITEM_CHRISTMAS_SOCK), 1);
            }
            InventoryService.gI().sendItemBags(player);

            if (!isSpecialItemInSpecialMap(itemMap)) {
                removeItemMap(itemMap);
            }

            TaskService.gI().checkDoneTaskPickItem(player, itemMap);
            TaskService.gI().checkDoneSideTaskPickItem(player, itemMap);
            TaskService.gI().checkDoneClanTaskPickItem(player, itemMap);

        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }

    private void sendItemPickMessage(Message msg, Item item, Player player) throws IOException {
        int itemType = item.template.type;
        switch (itemType) {
            case 9, 10, 34 -> {
                msg.writer().writeUTF(item.quantity > Short.MAX_VALUE ? "Bạn vừa nhận được " + Util.formatNumber(item.quantity) + " " + item.template.name : "");
                PlayerService.gI().sendInfoHpMpMoney(player);
            }
            default -> {
                switch (item.template.id) {
                    case ITEM_NO_NAME -> msg.writer().writeUTF("");
                    case ITEM_APPLE -> msg.writer().writeUTF("Bạn mới vừa ăn " + item.template.name);
                    case ITEM_BABY -> msg.writer().writeUTF("Wow, một cậu bé dễ thương!");
                    default -> {
                        if (itemType >= 0 && itemType < 5) {
                            msg.writer().writeUTF("Bạn nhận được " + item.template.name);
                        }
                    }
                }
            }
        }
        msg.writer().writeShort(item.quantity > Short.MAX_VALUE ? 9999 : item.quantity);
    }

    private boolean isSpecialItemInSpecialMap(ItemMap itemMap) {
        boolean isAppleMap = this.map.mapId >= ConstMap.NHA_GOHAN && this.map.mapId <= ConstMap.NHA_BROLY && itemMap.itemTemplate.id == ITEM_APPLE;
        boolean isBabyMap = this.map.mapId >= ConstMap.VACH_NUI_ARU_42 && this.map.mapId <= ConstMap.VACH_NUI_KAKAROT && itemMap.itemTemplate.id == ITEM_BABY;
        return isAppleMap || isBabyMap;
    }

    public void addItem(ItemMap itemMap) {
        if (itemMap != null && !items.contains(itemMap)) {
            items.add(0, itemMap);
        }
    }

    public void removeItemMap(ItemMap itemMap) {
        this.items.remove(itemMap);
    }

    public Player getRandomPlayerInMap() {
        List<Player> plNotVoHinh = new ArrayList<>();
        for (Player pl : this.notBosses) {
            if (pl != null && (pl.effectSkin == null || !pl.effectSkin.isVoHinh) && pl.maBuHold == null && !pl.isMabuHold) {
                plNotVoHinh.add(pl);
            }
        }
        if (!plNotVoHinh.isEmpty()) {
            return plNotVoHinh.get(Util.nextInt(0, plNotVoHinh.size() - 1));
        }
        return null;
    }

    public void load_Me_To_Another(Player player) {
        if (player.zone == null) return;
        try {
            if (MapService.gI().isMapOffline(this.map.mapId)) {
                if (player instanceof TrainingBoss || player instanceof NonInteractiveNPC) {
                    for (int i = players.size() - 1; i >= 0; i--) {
                        Player pl = players.get(i);
                        if (!player.equals(pl) && (player instanceof NonInteractiveNPC || ((TrainingBoss) player).playerAtt.equals(pl))) {
                            infoPlayer(pl, player);
                        }
                    }
                }
            } else {
                for (int i = players.size() - 1; i >= 0; i--) {
                    Player pl = players.get(i);
                    if (!player.equals(pl)) {
                        infoPlayer(pl, player);
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }

    public void load_Another_To_Me(Player player) {
        try {
            if (MapService.gI().isMapOffline(this.map.mapId)) {
                for (int i = this.humanoids.size() - 1; i >= 0; i--) {
                    Player pl = this.humanoids.get(i);
                    if (pl != null && (pl instanceof NonInteractiveNPC || pl instanceof TrainingBoss && ((TrainingBoss) pl).playerAtt.equals(player))) {
                        infoPlayer(player, pl);
                    }
                }
            } else {
                for (int i = this.humanoids.size() - 1; i >= 0; i--) {
                    Player pl = this.humanoids.get(i);
                    if (pl != null && !player.equals(pl)) {
                        infoPlayer(player, pl);
                    }
                }
            }
        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }

    public void loadBoss(Boss boss) {
        try {
            for (Player pl : this.bosses) {
                if (boss.equals(pl)) continue;
                
                if (MapService.gI().isMapOffline(this.map.mapId)) {
                    if (!pl.isPl() && !pl.isPet && !pl.isNewPet) {
                        infoPlayer(boss, pl);
                        infoPlayer(pl, boss);
                    }
                } else {
                    infoPlayer(boss, pl);
                    infoPlayer(pl, boss);
                }
            }
        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }

    private void infoPlayer(Player plReceive, Player plInfo) {
        try {
            Message msg = new Message(CMD_INFO_PLAYER);
            msg.writer().writeInt((int) plInfo.id);
            
            if (plInfo.clan != null) {
                msg.writer().writeInt(plInfo.clan.id);
            } else if (plInfo.isBoss && (plInfo.id == BossID.MABU || plInfo.id == BossID.SUPERBU)) {
                msg.writer().writeInt(-100);
            } else if (plInfo.isCopy) {
                msg.writer().writeInt(-2);
            } else {
                msg.writer().writeInt(-1);
            }
            
            msg.writer().writeByte(Service.gI().getCurrLevel(plInfo));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(plInfo.typePk);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeShort(plInfo.getHead());
            msg.writer().writeUTF(Service.gI().name(plInfo));
            msg.writer().writeInt(plInfo.nPoint.hp);
            msg.writer().writeInt(plInfo.nPoint.hpMax);
            msg.writer().writeShort(plInfo.getBody());
            msg.writer().writeShort(plInfo.getLeg());
            
            int flagbag = plInfo.getFlagBag();
            if (plReceive.isPl() && plReceive.getSession() != null && plReceive.getSession().version >= 228) {
                if (flagbag == 83) flagbag = 205;
            }
            
            msg.writer().writeByte(flagbag);
            msg.writer().writeByte(-1);
            msg.writer().writeShort(plInfo.location.x);
            msg.writer().writeShort(plInfo.location.y);
            msg.writer().writeShort(0); 
            msg.writer().writeShort(0); 
            msg.writer().writeByte(0); 
            msg.writer().writeByte(plInfo.idMark.getIdSpaceShip());
            msg.writer().writeByte(plInfo.effectSkill != null && plInfo.effectSkill.isMonkey ? 1 : 0);
            msg.writer().writeShort(plInfo.getMount());
            msg.writer().writeByte(plInfo.cFlag);
            msg.writer().writeByte(0);
            msg.writer().writeShort(plInfo.getAura());
            msg.writer().writeByte(plInfo.getEffFront());
            msg.writer().writeShort(plInfo.getHat());

            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Service.gI().sendFlagPlayerToMe(plReceive, plInfo);
        
        if (plInfo.isPl() && plInfo.effectSkill != null && plInfo.effectSkill.isChibi) {
            Service.gI().sendChibiFollowToMe(plReceive, plInfo);
        }

        if (plInfo.isDie()) {
            try {
                Message msgDie = new Message(CMD_PLAYER_DIE);
                msgDie.writer().writeInt((int) plInfo.id);
                msgDie.writer().writeByte(0);
                msgDie.writer().writeShort(plInfo.location.x);
                msgDie.writer().writeShort(plInfo.location.y);
                plReceive.sendMessage(msgDie);
                msgDie.cleanup();
            } catch (Exception ignored) {}
        }
    }

    public void mapInfo(Player pl) {
        try {
            Message msg = new Message(CMD_MAP_INFO);
            msg.writer().writeByte(this.map.mapId);
            msg.writer().writeByte(this.map.planetId);
            msg.writer().writeByte(this.map.tileId);
            msg.writer().writeByte(this.map.bgId);
            msg.writer().writeByte(this.map.type);
            msg.writer().writeUTF(this.map.mapName);
            msg.writer().writeByte(this.zoneId);
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);

            writeWayPoints(msg);
            writeMobs(msg);
            msg.writer().writeByte(0);
            
            writeNpcs(pl, msg);
            writeItems(pl, msg);
            writeBackground(msg);
            writeEffect(msg);

            msg.writer().writeByte(this.map.bgType);
            msg.writer().writeByte(pl.idMark.getIdSpaceShip());
            msg.writer().writeByte(this.map.mapId == ConstMap.LAU_DAI_LYCHEE ? 1 : 0);
            
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }

    private void writeWayPoints(Message msg) {
        try {
            List<WayPoint> wayPoints = this.map.wayPoints;
            msg.writer().writeByte(wayPoints.size());
            for (WayPoint wp : wayPoints) {
                msg.writer().writeShort(wp.minX);
                msg.writer().writeShort(wp.minY);
                msg.writer().writeShort(wp.maxX);
                msg.writer().writeShort(wp.maxY);
                msg.writer().writeBoolean(wp.isEnter);
                msg.writer().writeBoolean(wp.isOffline);
                msg.writer().writeUTF(wp.name);
            }
        } catch (Exception e) {
            try { msg.writer().writeByte(0); } catch (Exception ignored) {}
        }
    }

    private void writeMobs(Message msg) {
        try {
            List<Mob> activeMobs = new ArrayList<>();
            for (Mob mob : this.mobs) {
                if (mob.isBigBoss() && mob.tempId != MOB_HIRUDEGARN_PART && mob.isDie()) continue;
                activeMobs.add(mob);
            }
            msg.writer().writeByte(activeMobs.size());
            for (Mob mob : activeMobs) {
                msg.writer().writeBoolean(false); 
                msg.writer().writeBoolean(false); 
                msg.writer().writeBoolean(false); 
                msg.writer().writeBoolean(false); 
                msg.writer().writeBoolean(false); 
                msg.writer().writeByte(mob.tempId);
                msg.writer().writeByte(0); 
                msg.writer().writeInt(mob.point.gethp());
                msg.writer().writeByte(mob.level);
                msg.writer().writeInt((mob.point.getHpFull()));
                msg.writer().writeShort(mob.location.x);
                msg.writer().writeShort(mob.location.y);
                msg.writer().writeByte(mob.status);
                msg.writer().writeByte(mob.lvMob);
                msg.writer().writeBoolean(mob.tempId == ConstMob.GAU_TUONG_CUOP || (mob.tempId >= ConstMob.VOI_CHIN_NGA && mob.tempId <= ConstMob.PIANO));
            }
        } catch (Exception e) {
            try { msg.writer().writeByte(0); } catch (Exception ignored) {}
        }
    }

    private void writeNpcs(Player pl, Message msg) {
        try {
            List<Npc> npcs = NpcManager.getNpcsByMapPlayer(pl);
            msg.writer().writeByte(npcs.size());
            for (Npc npc : npcs) {
                msg.writer().writeByte(npc.status);
                msg.writer().writeShort(npc.cx);
                msg.writer().writeShort(npc.cy);
                msg.writer().writeByte(npc.tempId);
                msg.writer().writeShort(npc.avartar);
            }
        } catch (Exception e) {
            try { msg.writer().writeByte(0); } catch (Exception ignored) {}
        }
    }

    private void writeItems(Player pl, Message msg) {
        try {
            List<ItemMap> itemsMap = this.getItemMapsForPlayer(pl);
            msg.writer().writeByte(itemsMap.size());
            for (ItemMap it : itemsMap) {
                msg.writer().writeShort(it.itemMapId);
                msg.writer().writeShort(it.itemTemplate.id);
                msg.writer().writeShort(it.x);
                msg.writer().writeShort(it.y);
                msg.writer().writeInt((int) it.playerId);
            }
        } catch (Exception e) {
            try { msg.writer().writeByte(0); } catch (Exception ignored) {}
        }
    }

    private void writeBackground(Message msg) {
        try {
            final byte[] bgItem = FileIO.readFile("data/map/item_bg_map_data/" + this.map.mapId);
            msg.writer().write(bgItem);
        } catch (Exception e) {
            try { msg.writer().writeShort(0); } catch (Exception ignored) {}
        }
    }

    private void writeEffect(Message msg) {
        try {
            final byte[] effItem = FileIO.readFile("data/map/eff_map/" + this.map.mapId);
            msg.writer().write(effItem);
        } catch (Exception e) {
            try { msg.writer().writeShort(0); } catch (Exception ignored) {}
        }
    }

    public TrapMap isInTrap(Player player) {
        for (TrapMap trap : this.trapMaps) {
            if (player.location.x >= trap.x && player.location.x <= trap.x + trap.w
                    && player.location.y >= trap.y && player.location.y <= trap.y + trap.h) {
                return trap;
            }
        }
        return null;
    }

    public void sendBigBoss(Player player) {
        for (Mob mob : this.mobs) {
            if (!mob.isDie() && mob.tempId == ConstMob.HIRUDEGARN) {
                if (mob.lvMob >= 1) Service.gI().sendBigBoss2(player, 6, mob);
                if (mob.lvMob >= 2) Service.gI().sendBigBoss2(player, 5, mob);
                break;
            }
        }
    }

    public MaBuHold getMaBuHold() {
        for (MaBuHold hold : MapService.gI().getMapById(ConstMap.BUNG_MABU).zones.get(this.zoneId).maBuHolds) {
            if (hold.player == null) return hold;
        }
        return null;
    }

    public void setMaBuHold(int slot, int zoneId, Player player) {
        MapService.gI().getMapById(ConstMap.BUNG_MABU).zones.get(zoneId).maBuHolds.set(slot, new MaBuHold(slot, player));
    }

    public boolean isKhongCoTrongTaiTrongKhu() {
        for (Player pl : players) {
            if (!pl.isPl()) return false;
            
            int mId = pl.zone.map.mapId;
            if ((mId >= ConstMap.NHA_GOHAN && mId <= ConstMap.NHA_BROLY)
                    || mId == MAX_MAP_170
                    || mId == ConstMap.LANH_DIA_BANG_HOI
                    || mId == ConstMap.DAI_HOI_VO_THUAT
                    || mId == ConstMap.DAI_HOI_VO_THUAT_113
                    || mId == ConstMap.DAI_HOI_VO_THUAT_129
                    || MapService.gI().isMapDoanhTrai(mId)
                    || MapService.gI().isMapBlackBallWar(mId)
                    || MapService.gI().isMapBanDoKhoBau(mId)
                    || MapService.gI().isMapPhoBan(mId)
                    || MapService.gI().isMapMaBu(mId)
                    || MapService.gI().isMapKhiGasHuyDiet(mId)
                    || MapService.gI().isMapConDuongRanDoc(mId)
                    || MapService.gI().isMapOffline(mId)) {
                return false;
            }
        }
        return true;
    }
}