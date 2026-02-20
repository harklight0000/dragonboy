package server;

import network.SessionService;
import services.dungeon.BlackBallWarService;
import services.TrainingService;
import boss.Boss;
import boss.BossManager.BossManager;
import services.CombineService;
import consts.*;
import item.ItemData;
import player.PlayerDAO;
import matches.SuperRank.SuperRankDAO;
import services.ItemTimeService;
import services.ConsignShopService;
import matches.PVPService;
import matches.SuperRank.SuperRankService;
import network.Message;
import network.MySession;
import network.inetwork.IMessageHandler;
import network.inetwork.ISession;
import npc.MenuController;
import player.Player;
import services.ClanService;
import services.map.ChangeMapService;
import services.map.ItemMapService;
import services.map.MapService;
import services.map.NpcManager;
import services.player.FriendAndEnemyService;
import services.IntrinsicService;
import services.PlayerService;
import radar.Card;
import services.*;
import services.func.Input;
import services.func.LuckyRound;
import services.func.TransactionService;
import services.func.UseItem;
import shop.ShopService;
import player.skill.Skill;
import logger.MyLogger;
import services.TaskService;
import utils.Util;

import java.io.IOException;

public class Controller implements IMessageHandler {

    private static final byte CMD_CONSIGN_SHOP = -100;
    private static final byte CMD_RADAR = 127;
    private static final byte CMD_SPECIAL_MAP_CHANGE = -105;
    private static final byte CMD_LUCKY_ROUND_1 = 42;
    private static final byte CMD_LUCKY_ROUND_2 = -127;
    private static final byte CMD_INPUT = -125;
    private static final byte CMD_INTRINSIC = 112;
    private static final byte CMD_MAGIC_TREE = -34;
    private static final byte CMD_ENEMY = -99;
    private static final byte CMD_YARDRAT_MOVE = 18;
    private static final byte CMD_PRIVATE_CHAT = -72;
    private static final byte CMD_FRIEND = -80;
    private static final byte CMD_PVP = -59;
    private static final byte CMD_TRANSACTION = -86;
    private static final byte CMD_PET_INFO = -107;
    private static final byte CMD_PET_STATUS = -108;
    private static final byte CMD_BUY_ITEM = 6;
    private static final byte CMD_SELL_ITEM = 7;
    private static final byte CMD_ZONE_UI = 29;
    private static final byte CMD_CHANGE_ZONE = 21;
    private static final byte CMD_GLOBAL_CHAT = -71;
    private static final byte CMD_PLAYER_MENU = -79;
    private static final byte CMD_SKILL_SHORTCUT = -113;
    private static final byte CMD_NEW_GAME = -101;
    private static final byte CMD_FLAG_UI = -103;
    private static final byte CMD_MOVE = -7;
    private static final byte CMD_DOWNLOAD_DATA = -74;
    private static final byte CMD_COMBINE = -81;
    private static final byte CMD_UPDATE_DATA = -87;
    private static final byte CMD_ICON = -67;
    private static final byte CMD_IMAGE_BY_NAME = 66;
    private static final byte CMD_EFFECT_TEMPLATE = -66;
    private static final byte CMD_FLAG_CHOOSE = -62;
    private static final byte CMD_FLAG_EFFECT = -63;
    private static final byte CMD_ITEM_BG_TEMPLATE = -32;
    private static final byte CMD_NPC_DAU_THAN = 22;
    private static final byte CMD_CHANGE_MAP_WAYPOINT = -33;
    private static final byte CMD_CHANGE_MAP_WAYPOINT_2 = -23;
    private static final byte CMD_USE_SKILL = -45;
    private static final byte CMD_CLAN_INFO = -46;
    private static final byte CMD_CLAN_MESSAGE = -51;
    private static final byte CMD_CLAN_DONATE = -54;
    private static final byte CMD_CLAN_JOIN = -49;
    private static final byte CMD_CLAN_MEMBER_LIST = -50;
    private static final byte CMD_CLAN_REMOTE = -56;
    private static final byte CMD_CLAN_LIST = -47;
    private static final byte CMD_CLAN_LEAVE = -55;
    private static final byte CMD_CLAN_INVITE = -57;
    private static final byte CMD_USE_ITEM = -40;
    private static final byte CMD_CAPTION = -41;
    private static final byte CMD_DO_ITEM = -43;
    private static final byte CMD_CHANGE_MAP_SPECIAL = -91;
    private static final byte CMD_FINISH_LOAD_MAP = -39;
    private static final byte CMD_MOB_TEMPLATE = 11;
    private static final byte CMD_CHAT = 44;
    private static final byte CMD_SELECT_MENU = 32;
    private static final byte CMD_OPEN_MENU_NPC = 33;
    private static final byte CMD_SELECT_SKILL = 34;
    private static final byte CMD_ATTACK_MOB = 54;
    private static final byte CMD_ATTACK_PLAYER = -60;
    private static final byte CMD_VERSION_RES = -27;
    private static final byte CMD_IMAGE_VERSION = -111;
    private static final byte CMD_PICK_ITEM = -20;
    private static final byte CMD_NOT_MAP = -28;
    private static final byte CMD_NOT_LOGIN = -29;
    private static final byte CMD_SUB_COMMAND = -30;
    private static final byte CMD_GO_HOME = -15;
    private static final byte CMD_REVIVE = -16;
    private static final byte CMD_PROTECT_ACCOUNT = -104;
    private static final byte CMD_SUPER_RANK = -118;
    private static final byte CMD_FINISH_UPDATE = -38;
    private static final byte CMD_CONFIRM_ACHIEVEMENT = -76;
    private static final byte CMD_CHECK_MOVE = -78;

    private static final byte ACTION_CREATE_CHAR = 2;
    private static final byte ACTION_UPDATE_MAP = 6;
    private static final byte ACTION_UPDATE_SKILL = 7;
    private static final byte ACTION_UPDATE_ITEM = 8;
    private static final byte ACTION_MAP_TEMP = 10;
    private static final byte ACTION_INIT_INFO = 13;

    private static final byte ACTION_LOGIN = 0;
    private static final byte ACTION_CLIENT_INFO = 2;

    private static final byte SUB_CMD_INCREASE_POINT = 16;
    private static final byte SUB_CMD_SUB_MENU = 64;

    private static final int MAX_ERRORS_LOG = 5;
    private static final int MIN_CHAR_NAME_LENGTH = 5;
    private static final int MAX_CHAR_NAME_LENGTH = 10;

    private static Controller instance;
    private int errors;

    public static Controller gI() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    @Override
    public void onMessage(ISession s, Message _msg) {
        long st = System.currentTimeMillis();
        MySession _session = (MySession) s;
        Player player = _session.player;

        try {
            byte cmd = _msg.command;
            if (handleSessionCommands(_session, _msg, cmd)) {
                return;
            }

            if (player == null) return;
            handlePlayerCommands(player, _session, _msg, cmd);

        } catch (Exception e) {
            if (errors < MAX_ERRORS_LOG) {
                errors++;
                MyLogger.logError(e);
                MyLogger.logWarning("Lỗi function: 'onMessage'");
                MyLogger.logWarning("Lỗi controller message command: " + _msg.command);
            }
        } finally {
            _msg.cleanup();
            _msg.dispose();
        }
    }

    private boolean handleSessionCommands(MySession session, Message msg, byte cmd) throws Exception {
        switch (cmd) {
            case CMD_NOT_LOGIN -> messageNotLogin(session, msg);
            case CMD_NOT_MAP -> messageNotMap(session, msg);
            case CMD_SUB_COMMAND -> messageSubCommand(session, msg);
            case CMD_DOWNLOAD_DATA -> handleDownloadData(session, msg);
            case CMD_UPDATE_DATA -> SessionService.updateData(session);
            case CMD_ICON -> SessionService.sendIcon(session, msg.reader().readInt());
            case CMD_IMAGE_BY_NAME -> SessionService.sendImageByName(session, msg.reader().readUTF());
            case CMD_ITEM_BG_TEMPLATE -> SessionService.sendItemBGTemplate(session, msg.reader().readShort());
            case CMD_VERSION_RES -> {
                session.sendKey();
                SessionService.sendVersionRes(session);
            }
            case CMD_IMAGE_VERSION -> SessionService.sendDataImageVersion(session);
            case CMD_CAPTION -> Service.gI().sendCaption(session, msg.reader().readByte());
            case CMD_NEW_GAME -> newGame(session, msg);
            case CMD_MOB_TEMPLATE -> SessionService.requestMobTemplate(session, msg.reader().readByte());
            default -> {
                return false;
            }
        }
        return true;
    }

    private void handlePlayerCommands(Player player, MySession session, Message msg, byte cmd) throws IOException {
        switch (cmd) {
            case CMD_CONSIGN_SHOP -> handleConsignShop(player, msg);
            case CMD_RADAR -> handleRadar(player, msg);
            case CMD_SPECIAL_MAP_CHANGE -> handleSpecialMapChange(player);
            case CMD_LUCKY_ROUND_1, CMD_LUCKY_ROUND_2 -> LuckyRound.gI().readOpenBall(player, msg);
            case CMD_INPUT -> Input.gI().doInput(player, msg);
            case CMD_INTRINSIC -> IntrinsicService.gI().showMenu(player);
            case CMD_MAGIC_TREE -> handleMagicTree(player, msg);
            case CMD_ENEMY -> FriendAndEnemyService.gI().controllerEnemy(player, msg);
            case CMD_YARDRAT_MOVE -> {
                player.changeMapVIP = true;
                FriendAndEnemyService.gI().goToPlayerWithYardrat(player, msg);
            }
            case CMD_PRIVATE_CHAT -> FriendAndEnemyService.gI().chatPrivate(player, msg);
            case CMD_FRIEND -> FriendAndEnemyService.gI().controllerFriend(player, msg);
            case CMD_PVP -> {
                if (!isAccountProtected(player)) PVPService.gI().controllerThachDau(player, msg);
            }
            case CMD_TRANSACTION -> TransactionService.gI().controller(player, msg);
            case CMD_PET_INFO -> Service.gI().showInfoPet(player);
            case CMD_PET_STATUS -> {
                if (player.pet != null) player.pet.changeStatus(msg.reader().readByte());
            }
            case CMD_BUY_ITEM -> {
                if (!Maintenance.isRunning && isSecureAndActionable(player)) {
                    ShopService.gI().takeItem(player, msg.reader().readByte(), msg.reader().readShort());
                }
            }
            case CMD_SELL_ITEM -> {
                if (!Maintenance.isRunning && isSecureAndActionable(player)) handleSellItem(player, msg);
            }
            case CMD_ZONE_UI -> ChangeMapService.gI().openZoneUI(player);
            case CMD_CHANGE_ZONE -> ChangeMapService.gI().changeZone(player, msg.reader().readByte());
            case CMD_GLOBAL_CHAT -> {
                if (!isTransactionBlocked(player)) ChatGlobalService.gI().chat(player, msg.reader().readUTF());
            }
            case CMD_PLAYER_MENU -> Service.gI().getPlayerMenu(player, msg.reader().readInt());
            case CMD_SKILL_SHORTCUT -> handleSkillShortcut(player, msg);
            case CMD_FLAG_UI -> handleFlagUI(player, msg);
            case CMD_MOVE -> handleMove(player, msg);
            case CMD_COMBINE -> handleCombine(player, msg);
            case CMD_EFFECT_TEMPLATE -> handleEffectTemplate(player, session, msg);
            case CMD_FLAG_CHOOSE -> FlagBagService.gI().sendIconFlagChoose(player, msg.reader().readByte());
            case CMD_FLAG_EFFECT -> FlagBagService.gI().sendIconEffectFlag(player, msg.reader().readByte() & 0xFF);
            case CMD_NPC_DAU_THAN -> {
                msg.reader().readByte();
                NpcManager.getNpc(ConstNpc.DAU_THAN).confirmMenu(player, msg.reader().readByte());
            }
            case CMD_CHANGE_MAP_WAYPOINT, CMD_CHANGE_MAP_WAYPOINT_2 -> {
                ChangeMapService.gI().changeMapWaypoint(player);
                Service.gI().hideWaitDialog(player);
            }
            case CMD_USE_SKILL -> {
                if (!isTransactionBlocked(player)) {
                    SkillService.gI().useSkill(player, null, null, msg.reader().readByte(), msg);
                }
            }
            case CMD_USE_ITEM -> {
                if (!isTransactionBlocked(player)) UseItem.gI().getItem(session, msg);
            }
            case CMD_DO_ITEM -> {
                if (isSecureAndActionable(player)) UseItem.gI().doItem(player, msg);
            }
            case CMD_CHANGE_MAP_SPECIAL -> handleChangeMapSpecial(player, msg);
            case CMD_FINISH_LOAD_MAP -> ChangeMapService.gI().finishLoadMap(player);
            case CMD_CHAT -> PlayerService.gI().chat(player, msg.reader().readUTF());
            case CMD_SELECT_MENU -> MenuController.gI().doSelectMenu(player, msg.reader().readShort(), msg.reader().readByte());
            case CMD_OPEN_MENU_NPC -> MenuController.gI().openMenuNPC(session, msg.reader().readShort(), player);
            case CMD_SELECT_SKILL -> SkillService.gI().selectSkill(player, msg.reader().readShort());
            case CMD_ATTACK_MOB -> handleAttackMob(player, msg);
            case CMD_ATTACK_PLAYER -> Service.gI().attackPlayer(player, msg.reader().readInt());
            case CMD_PICK_ITEM -> {
                if (!player.isDie()) ItemMapService.gI().pickItem(player, msg.reader().readShort(), false);
            }
            case CMD_GO_HOME -> handleGoHome(player);
            case CMD_REVIVE -> {
                if (!player.isPKDHVT) PlayerService.gI().hoiSinh(player);
            }
            case CMD_PROTECT_ACCOUNT -> Service.gI().mabaove(player, msg.reader().readInt());
            case CMD_SUPER_RANK -> handleSuperRank(player, msg);
            case CMD_FINISH_UPDATE -> finishUpdate(player);
            case CMD_CONFIRM_ACHIEVEMENT -> AchievementService.gI().confirmAchievement(player, msg.reader().readByte());
            case CMD_CLAN_INFO, CMD_CLAN_MESSAGE, CMD_CLAN_DONATE, CMD_CLAN_JOIN, CMD_CLAN_MEMBER_LIST, 
                 CMD_CLAN_REMOTE, CMD_CLAN_LIST, CMD_CLAN_LEAVE, CMD_CLAN_INVITE -> handleClanCommands(player, msg, cmd);
            case CMD_CHECK_MOVE -> msg.reader().readInt();
        }
    }

    private boolean isTransactionBlocked(Player player) {
        if (TransactionService.gI().check(player)) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return true;
        }
        return false;
    }

    private boolean isAccountProtected(Player player) {
        if (player.baovetaikhoan) {
            Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
            return true;
        }
        return false;
    }

    private boolean isSecureAndActionable(Player player) {
        return !isTransactionBlocked(player) && !isAccountProtected(player);
    }

    private void handleDownloadData(MySession session, Message msg) throws IOException {
        byte type = msg.reader().readByte();
        if (type == 1) {
            SessionService.sendSizeRes(session);
        } else if (type == 2) {
            SessionService.sendRes(session);
        }
    }

    private void handleConsignShop(Player player, Message msg) throws IOException {
        if (!isSecureAndActionable(player)) return;

        byte action = msg.reader().readByte();
        switch (action) {
            case 0 -> {
                short idItem = msg.reader().readShort();
                byte moneyType = msg.reader().readByte();
                int money = msg.reader().readInt();
                int quantity = (player.getSession().version >= 222) ? msg.reader().readInt() : msg.reader().readByte();
                if (quantity > 0) ConsignShopService.gI().KiGui(player, idItem, money, moneyType, quantity);
            }
            case 1, 2 -> ConsignShopService.gI().claimOrDel(player, action, msg.reader().readShort());
            case 3 -> {
                short idItem = msg.reader().readShort();
                msg.reader().readByte();
                msg.reader().readInt();
                ConsignShopService.gI().buyItem(player, idItem);
            }
            case 4 -> ConsignShopService.gI().openShopKyGui(player, msg.reader().readByte(), msg.reader().readByte());
            case 5 -> ConsignShopService.gI().upItemToTop(player, msg.reader().readShort());
            default -> Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void handleRadar(Player player, Message msg) throws IOException {
        byte actionRadar = msg.reader().readByte();
        switch (actionRadar) {
            case 0 -> RadarService.gI().sendRadar(player, player.Cards);
            case 1 -> {
                short idC = msg.reader().readShort();
                Card card = player.Cards.stream().filter(r -> r != null && r.Id == idC).findFirst().orElse(null);
                if (card != null && card.Level != 0) {
                    if (card.Used == 0) {
                        if (player.Cards.stream().anyMatch(c -> c != null && c.Used == 1)) {
                            Service.gI().sendThongBao(player, "Số thẻ sử dụng đã đạt tối đa");
                            return;
                        }
                        card.Used = 1;
                    } else {
                        card.Used = 0;
                    }
                    RadarService.gI().Radar1(player, idC, card.Used);
                    Service.gI().point(player);
                }
            }
        }
    }

    private void handleSpecialMapChange(Player player) {
        if (player.type == 0 && player.maxTime == 30) {
            ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.NHA_BUNMA, -1, Util.nextInt(60, 200));
            player.idMark.setGotoFuture(false);
            return;
        }
        
        if (player.type == 1 && player.maxTime == 5 && player.idMark != null && player.idMark.isGoToBDKB()) {
            ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, ConstMap.DONG_HAI_TAC, -1), 35, 35);
            player.idMark.setGoToBDKB(false);
            return;
        }
        
        if (player.type == 2 && player.maxTime == 5) {
            int targetMap = MapService.gI().isMapHanhTinhThucVat(player.zone.map.mapId) ? ConstMap.NUI_KHI_VANG : ConstMap.KHU_HANG_DONG;
            ChangeMapService.gI().changeMap(player, targetMap, -1, -1, 5);
            return;
        }
        
        if (player.type == 3 && player.maxTime == 5) {
            ChangeMapService.gI().changeMap(player, player.idMark.getZoneKhiGasHuyDiet(), player.idMark.getXMapKhiGasHuyDiet(), player.idMark.getYMapKhiGasHuyDiet());
            player.idMark.setZoneKhiGasHuyDiet(null);
            return;
        }
        
        if (player.type == 4 && player.maxTime == 5 && player.idMark != null && player.idMark.isGoToKGHD()) {
            ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, ConstMap.THANH_PHO_SANTA_149, -1), 100 + (Util.nextInt(-10, 10)), 336);
            player.idMark.setGoToKGHD(false);
            return;
        }
        
        if (player.type == 5 && player.maxTime == 5) {
            ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, ConstMap.TAY_THANH_DIA, -1), 100 + (Util.nextInt(-10, 10)), 336);
        }
    }

    private void handleMagicTree(Player player, Message msg) throws IOException {
        switch (msg.reader().readByte()) {
            case 1 -> player.magicTree.openMenuTree();
            case 2 -> player.magicTree.loadMagicTree();
        }
    }

    private void handleSellItem(Player player, Message msg) throws IOException {
        byte action = msg.reader().readByte();
        byte type = msg.reader().readByte();
        short id = msg.reader().readShort();
        if (action == 0) {
            ShopService.gI().showConfirmSellItem(player, type, id);
        } else {
            ShopService.gI().sellItem(player, type, id);
        }
    }

    private void handleSkillShortcut(Player player, Message msg) {
        for (int i = 0; i < 10; i++) {
            try {
                player.playerSkill.skillShortCut[i] = msg.reader().readByte();
            } catch (IOException e) {
                player.playerSkill.skillShortCut[i] = -1;
            }
        }
        player.playerSkill.sendSkillShortCut();
    }

    private void handleFlagUI(Player player, Message msg) throws IOException {
        switch (msg.reader().readByte()) {
            case 0 -> Service.gI().openFlagUI(player);
            case 1 -> Service.gI().chooseFlag(player, msg.reader().readByte());
        }
    }

    private void handleMove(Player player, Message msg) {
        if (player.isDie()) {
            Service.gI().charDie(player);
            return;
        }
        if (player.effectSkill.isHaveEffectSkill()) return;

        int toX = player.location.x;
        int toY = player.location.y;
        try {
            byte b = msg.reader().readByte();
            toX = msg.reader().readShort();
            toY = msg.reader().readShort();
            
            if (player.zone != null && MapService.gI().isMapBlackBallWar(player.zone.map.mapId) 
                    && Util.getDistance(player.location.x, player.location.y, toX, toY) > 500) {
                return;
            }
            if (b == 1) {
                AchievementService.gI().checkDoneTaskFly(player, player.location.x - toX);
            }
        } catch (IOException ignored) {}
        
        PlayerService.gI().playerMove(player, toX, toY);
    }

    private void handleCombine(Player player, Message msg) throws IOException {
        msg.reader().readByte();
        int[] indexItem = new int[msg.reader().readByte()];
        for (int i = 0; i < indexItem.length; i++) {
            indexItem[i] = msg.reader().readByte();
        }
        CombineService.gI().showInfoCombine(player, indexItem);
    }

    private void handleEffectTemplate(Player player, MySession session, Message msg) throws IOException {
        int effId = msg.reader().readShort();
        
        if (player.zone == null) return; 

        int idT = effId;
        int shenronType = player.zone.shenronType;

        if (idT == 25 && shenronType != -1 
                && player.zone.map.mapId != ConstMap.LANG_ARU 
                && player.zone.map.mapId != ConstMap.LANG_MORI 
                && player.zone.map.mapId != ConstMap.LANG_KAKAROT) {
            
            idT = (shenronType == 0 || shenronType == 1) ? 59 : 60; 
        }
        
        SessionService.sendEffectTemplate(session, effId, idT);
    }


    private void handleChangeMapSpecial(Player player, Message msg) throws IOException {
        switch (player.idMark.getTypeChangeMap()) {
            case ConstMap.CHANGE_CAPSULE -> UseItem.gI().choseMapCapsule(player, msg.reader().readByte());
            case ConstMap.CHANGE_BLACK_BALL -> BlackBallWarService.gI().changeMap(player, msg.reader().readByte());
        }
    }

    private void handleAttackMob(Player player, Message msg) throws IOException {
        int mobId = msg.reader().readByte();
        int masterId = -1;
        boolean isMobMe = mobId == -1;
        if (isMobMe) masterId = msg.reader().readInt();
        Service.gI().attackMob(player, mobId, isMobMe, masterId);
    }

    private void handleGoHome(Player player) {
        int mapId = MapService.gI().isMapMaBu(player.zone.map.mapId) ? ConstMap.CONG_PHI_THUYEN : player.gender + ConstMap.NHA_GOHAN;
        ChangeMapService.gI().changeMapBySpaceShip(player, mapId, 0, -1);
    }

    private void handleSuperRank(Player player, Message msg) throws IOException {
        int id = msg.reader().readInt();
        switch (player.idMark.getMenuType()) {
            case 0, 1, 2 -> SuperRankService.gI().competing(player, id);
            default -> {
                if (player.isAdmin()) {
                    Boss boss = BossManager.gI().getBoss(id);
                    if (boss != null) ChangeMapService.gI().changeMapYardrat(player, boss.zone, boss.location.x, boss.location.y);
                } else {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                }
            }
        }
    }

    private void handleClanCommands(Player player, Message msg, byte cmd) throws IOException {
        switch (cmd) {
            case CMD_CLAN_INFO -> ClanService.gI().getClan(player, msg);
            case CMD_CLAN_MESSAGE -> ClanService.gI().clanMessage(player, msg);
            case CMD_CLAN_DONATE -> ClanService.gI().clanDonate(player, msg);
            case CMD_CLAN_JOIN -> ClanService.gI().joinClan(player, msg);
            case CMD_CLAN_MEMBER_LIST -> ClanService.gI().sendListMemberClan(player, msg.reader().readInt());
            case CMD_CLAN_REMOTE -> ClanService.gI().clanRemote(player, msg);
            case CMD_CLAN_LIST -> ClanService.gI().sendListClan(player, msg.reader().readUTF());
            case CMD_CLAN_LEAVE -> ClanService.gI().showMenuLeaveClan(player);
            case CMD_CLAN_INVITE -> ClanService.gI().clanInvite(player, msg);
        }
    }

    public void messageNotLogin(MySession session, Message msg) {
        if (msg == null) return;
        try {
            switch (msg.reader().readByte()) {
                case ACTION_LOGIN -> session.login(msg.reader().readUTF(), msg.reader().readUTF());
                case ACTION_CLIENT_INFO -> Service.gI().setClientType(session, msg);
            }
        } catch (IOException e) {
            session.disconnect();
        }
    }

    public void messageNotMap(MySession session, Message msg) {
        if (msg == null) return;
        Player player = session.player;
        try {
            switch (msg.reader().readByte()) {
                case ACTION_CREATE_CHAR -> createChar(session, msg);
                case ACTION_UPDATE_MAP -> SessionService.updateMap(session);
                case ACTION_UPDATE_SKILL -> SessionService.updateSkill(session);
                case ACTION_UPDATE_ITEM -> ItemData.updateItem(session);
                case ACTION_MAP_TEMP -> SessionService.sendMapTemp(session, msg.reader().readUnsignedByte());
                case ACTION_INIT_INFO -> {
                    if (player != null && player.isPl()) initPlayerInfo(player);
                }
            }
        } catch (IOException e) {
            MyLogger.logError(e);
        }
    }

    private void initPlayerInfo(Player player) {
        Service.gI().player(player);
        Service.gI().Send_Caitrang(player);
        Service.gI().sendFlagBag(player);
        player.playerSkill.sendSkillShortCut();
        ItemTimeService.gI().sendAllItemTime(player);
        TaskService.gI().sendInfoCurrentTask(player);
        Service.gI().sendThongBaoFromAdmin(player, "thong bao abcd\n");

        if (TaskService.gI().getIdTask(player) == ConstTask.TASK_0_0) {
            TaskService.gI().sendFirstTask(player);
        }

        if (player.inventory.itemsBody.get(10).isNotNullItem()) {
            Service.gI().sendChibi(player);
        }
        player.zone.mapInfo(player);
        
        if (player.getSession().version >= 231) {
            for (Skill skill : player.playerSkill.skills) {
                if (skill.currLevel > 0 && skill.template.type == 4) {
                    SkillService.gI().sendCurrLevelSpecial(player, skill);
                }
            }
        }
        
        Service.gI().sendTimeSkill(player);
        player.sendNewPet();
        TrainingService.gI().tnsmLuyenTapUp(player);
        
        if (player.getSession() != null && player.getSession().tongnap > 0) {
            AchievementService.gI().checkDoneTask(player, ConstAchievement.LAN_DAU_NAP_NGOC);
        }
    }

    public void messageSubCommand(MySession session, Message msg) {
        if (msg == null) return;
        Player player = session.player;
        try {
            switch (msg.reader().readByte()) {
                case SUB_CMD_INCREASE_POINT -> {
                    byte type = msg.reader().readByte();
                    short point = msg.reader().readShort();
                    if (player != null && player.nPoint != null) {
                        player.nPoint.increasePoint(type, point);
                    }
                }
                case SUB_CMD_SUB_MENU -> SubMenuService.gI().controller(player, msg.reader().readInt(), msg.reader().readShort());
            }
        } catch (IOException e) {
            MyLogger.logError(e);
        }
    }

    public void createChar(MySession session, Message msg) {
        if (Maintenance.isRunning) return;
        
        boolean created = false;
        try {
            String name = msg.reader().readUTF();
            int gender = msg.reader().readByte();
            int hair = msg.reader().readByte();
            
            if (name.length() < MIN_CHAR_NAME_LENGTH || name.length() > MAX_CHAR_NAME_LENGTH) {
                Service.gI().sendThongBaoOK(session, "Tên nhân vật chỉ đồng ý các ký tự a-z, 0-9 và chiều dài từ " + MIN_CHAR_NAME_LENGTH + " đến " + MAX_CHAR_NAME_LENGTH + " ký tự");
                return;
            }

            if (PlayerDAO.usernameExists(name)) {
                Service.gI().sendThongBaoOK(session, "Tên nhân vật đã tồn tại");
                return;
            }

            if (Util.haveSpecialCharacter(name)) {
                Service.gI().sendThongBaoOK(session, "Tên nhân vật không được chứa ký tự đặc biệt");
                return;
            }

            for (String ignoredName : ConstIgnoreName.IGNORE_NAME) {
                if (name.equals(ignoredName)) {
                    Service.gI().sendThongBaoOK(session, "Tên nhân vật đã tồn tại");
                    return;
                }
            }

            created = PlayerDAO.createNewPlayer(session.userId, name.toLowerCase(), (byte) gender, hair);
            
        } catch (Exception e) {
            MyLogger.logError(e);
        }
        
        if (created) {
            session.login(session.uu, session.pp);
        }
    }

    public void newGame(MySession session, Message msg) {
        Service.gI().sendThongBaoOK(session, "Truy Cập: " + DragonBoy.DOMAIN + "\n Đề Đăng Ký & Tải Game");
    }

    public void sendInfo(MySession session) {
        try {
            Player player = session.player;
            SessionService.sendTileSetInfo(session);
            IntrinsicService.gI().sendInfoIntrinsic(player);
            Service.gI().point(player);
            TaskService.gI().sendTaskMain(player);
            Service.gI().clearMap(player);
            ClanService.gI().sendMyClan(player);
            PlayerService.gI().sendMaxStamina(player);
            PlayerService.gI().sendCurrentStamina(player);
            Service.gI().sendNangDong(player);
            Service.gI().sendHavePet(player);
            Service.gI().sendTopRank(player);
            
            if (player.superRank != null && player.superRank.rank < 1) {
                player.superRank.rank = SuperRankDAO.getRank((int) player.id);
                player.superRank.lastRewardTime = System.currentTimeMillis();
                SuperRankDAO.insertData(player);
            }
            
            ServerNotify.gI().sendNotifyTab(player);
            player.setClothes.setup();
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            ItemTimeService.gI().sendCanAutoPlay(player);
            player.start();
        } catch (Exception ignored) {
        }
    }

    public void finishUpdate(Player player) {
        if (player.getSession() != null) {
            player.getSession().finishUpdate = true;
        }
    }
}