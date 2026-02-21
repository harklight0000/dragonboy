package services;

import boss.Boss;
import boss.BossID;
import clan.ClanMember;
import consts.*;
import item.Item;
import map.ItemMap;
import map.Zone;
import mob.Mob;
import network.Message;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import server.Client;
import server.GameData;
import server.DragonBoy;
import logger.MyLogger;
import task.ClanTaskTemplate;
import task.SideTaskTemplate;
import task.SubTaskMain;
import task.TaskMain;
import utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TaskService {

    private static final byte NMEMBER_DO_TASK_TOGETHER = 2;
    private static final int CMD_TASK_MAIN_INFO = 40;
    private static final int CMD_NEXT_SUB_TASK = 41;
    private static final int CMD_UPDATE_SUB_TASK_COUNT = 43;

    private static final int ITEM_CHICKEN_LEG = 73;
    private static final int ITEM_APPLE = 74;
    private static final int ITEM_BABY_MABU = 78;
    private static final int ITEM_RADAR = 380;
    private static final int ITEM_MAGIC_PEA_BOX = 77;
    private static final int ITEM_CHRISTMAS_TREE = 822;

    private static final int[] TASK_BASE_REWARDS = {500, 1000, 1200, 3000, 7000, 20000};

    private static final int[] TASK_MOB_MAPPING = {
        ConstMob.KHUNG_LONG, ConstMob.LON_LOI, ConstMob.QUY_DAT, ConstMob.KHUNG_LONG_ME,
        ConstMob.LON_LOI_ME, ConstMob.QUY_DAT_ME, ConstMob.THAN_LAN_BAY, ConstMob.PHI_LONG,
        ConstMob.QUY_BAY, ConstMob.THAN_LAN_ME, ConstMob.PHI_LONG_ME, ConstMob.QUY_BAY_ME,
        ConstMob.HEO_RUNG, ConstMob.HEO_DA_XANH, ConstMob.HEO_XAYDA, ConstMob.OC_MUON_HON,
        ConstMob.OC_SEN, ConstMob.HEO_XAYDA_ME, ConstMob.KHONG_TAC, ConstMob.QUY_DAU_TO,
        ConstMob.QUY_DIA_NGUC, ConstMob.HEO_RUNG_ME, ConstMob.HEO_XANH_ME, ConstMob.ALIEN,
        ConstMob.TAMBOURINE, ConstMob.DRUM, ConstMob.AKKUMAN, ConstMob.NAPPA,
        ConstMob.SOLDIER, ConstMob.APPULE, ConstMob.RASPBERRY, ConstMob.THAN_LAN_XANH,
        ConstMob.QUY_DAU_NHON, ConstMob.QUY_DAU_VANG, ConstMob.QUY_DA_TIM, ConstMob.QUY_GIA,
        ConstMob.CA_SAU, ConstMob.DOI_DA_XANH, ConstMob.QUY_CHIM, ConstMob.LINH_DAU_TROC,
        ConstMob.LINH_TAI_DAI, ConstMob.LINH_VU_TRU, ConstMob.KHI_LONG_DEN, ConstMob.KHI_GIAP_SAT,
        ConstMob.KHI_LONG_DO, ConstMob.KHI_LONG_VANG, ConstMob.XEN_CON_CAP_1, ConstMob.XEN_CON_CAP_2,
        ConstMob.XEN_CON_CAP_3, ConstMob.XEN_CON_CAP_4, ConstMob.XEN_CON_CAP_5, ConstMob.XEN_CON_CAP_6,
        ConstMob.XEN_CON_CAP_7, ConstMob.XEN_CON_CAP_8, ConstMob.TAI_TIM, ConstMob.ABO,
        ConstMob.KADO, ConstMob.DA_XANH
    };

    private static TaskService instance;

    public static TaskService gI() {
        if (instance == null) {
            instance = new TaskService();
        }
        return instance;
    }

    public TaskMain getTaskMainById(Player player, int id) {
        for (TaskMain tm : GameData.TASKS) {
            if (tm.id == id) {
                TaskMain newTaskMain = new TaskMain(tm);
                newTaskMain.detail = transformName(player, newTaskMain.detail);
                for (SubTaskMain stm : newTaskMain.subTasks) {
                    stm.mapId = (short) transformMapId(player, stm.mapId);
                    stm.npcId = (byte) transformNpcId(player, stm.npcId);
                    stm.notify = transformName(player, stm.notify);
                    stm.name = transformName(player, stm.name);
                }
                return newTaskMain;
            }
        }
        return player.playerTask.taskMain;
    }

    public void sendTaskMain(Player player) {
        Message msg = null;
        try {
            msg = new Message(CMD_TASK_MAIN_INFO);
            msg.writer().writeShort(player.playerTask.taskMain.id);
            msg.writer().writeByte(player.playerTask.taskMain.index);
            msg.writer().writeUTF(player.playerTask.taskMain.name);
            msg.writer().writeUTF(player.playerTask.taskMain.detail);
            msg.writer().writeByte(player.playerTask.taskMain.subTasks.size());

            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeUTF(stm.name);
                msg.writer().writeByte(stm.npcId);
                msg.writer().writeShort(stm.mapId);
                msg.writer().writeUTF(stm.notify.isEmpty() ? "" : stm.notify);
            }

            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeShort(stm.maxCount);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            MyLogger.logError(e, "Lỗi sendTaskMain của player: " + player.name);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendFirstTask(Player player) {
        NpcService.gI().createTutorial(player, -1,
                "Chào Mừng " + player.name + " Đến Với: " + DragonBoy.NAME + "\n"
                + "Nhiệm vụ đầu tiên của bạn là di chuyển\n"
                + "Bạn hãy di chuyển nhân vật theo mũi tên chỉ hướng");
    }

    public void sendNextTaskMain(Player player) {
        rewardDoneTask(player);
        int currentTaskId = player.playerTask.taskMain.id;

        switch (currentTaskId) {
            case 3 ->
                player.playerTask.taskMain = getTaskMainById(player, player.gender + 4);
            case 4, 5, 6 ->
                player.playerTask.taskMain = getTaskMainById(player, 7);
            default ->
                player.playerTask.taskMain = getTaskMainById(player, currentTaskId + 1);
        }

        sendTaskMain(player);
        Service.gI().sendThongBao(player, "Nhiệm vụ tiếp theo của bạn là " + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    public void sendUpdateCountSubTask(Player player) {
        Message msg = null;
        try {
            msg = new Message(CMD_UPDATE_SUB_TASK_COUNT);
            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            player.sendMessage(msg);
        } catch (IOException e) {
            MyLogger.logError(e, "Lỗi sendUpdateCountSubTask");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendNextSubTask(Player player) {
        Message msg = null;
        try {
            msg = new Message(CMD_NEXT_SUB_TASK);
            player.sendMessage(msg);
        } catch (Exception e) {
            MyLogger.logError(e, "Lỗi sendNextSubTask của player: " + player.name);
            Service.gI().sendThongBao(player, "Có lỗi xảy ra khi tải nhiệm vụ, vui lòng đăng nhập lại!");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendInfoCurrentTask(Player player) {
        Service.gI().sendThongBao(player, "Nhiệm vụ hiện tại của bạn là " + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    public boolean checkDoneTaskTalkNpc(Player player, Npc npc) {
        if (isMainNpcOfPlayer(player, npc.tempId)) {
            return checkMainNpcTasks(player);
        }

        switch (npc.tempId) {
            case ConstNpc.ONG_GOHAN, ConstNpc.ONG_MOORI, ConstNpc.ONG_PARAGUS -> {
                return doneTask(player, ConstTask.TASK_0_2) || doneTask(player, ConstTask.TASK_0_5)
                        || doneTask(player, ConstTask.TASK_1_1) || doneTask(player, ConstTask.TASK_2_1)
                        || doneTask(player, ConstTask.TASK_3_2) || doneTask(player, ConstTask.TASK_4_3)
                        || doneTask(player, ConstTask.TASK_5_3) || doneTask(player, ConstTask.TASK_6_3)
                        || doneTask(player, ConstTask.TASK_7_3) || doneTask(player, ConstTask.TASK_8_2)
                        || doneTask(player, ConstTask.TASK_11_3) || doneTask(player, ConstTask.TASK_12_1)
                        || doneTask(player, ConstTask.TASK_22_0);
            }
            case ConstNpc.DR_DRIEF, ConstNpc.CARGO, ConstNpc.CUI -> {
                return player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_17_1);
            }
            case ConstNpc.BUNMA, ConstNpc.DENDE, ConstNpc.APPULE -> {
                return doneTask(player, ConstTask.TASK_7_2);
            }
            case ConstNpc.BUNMA_TL -> {
                return doneTask(player, ConstTask.TASK_22_3) || doneTask(player, ConstTask.TASK_22_5)
                        || doneTask(player, ConstTask.TASK_23_4) || doneTask(player, ConstTask.TASK_24_4)
                        || doneTask(player, ConstTask.TASK_25_5) || doneTask(player, ConstTask.TASK_26_5)
                        || doneTask(player, ConstTask.TASK_27_5) || doneTask(player, ConstTask.TASK_28_5);
            }
            case ConstNpc.CALICK -> {
                return doneTask(player, ConstTask.TASK_22_1);
            }
            case ConstNpc.THAN_MEO_KARIN -> {
                return doneTask(player, ConstTask.TASK_27_0);
            }
            case ConstNpc.OSIN -> {
                return doneTask(player, ConstTask.TASK_28_0) || doneTask(player, ConstTask.TASK_28_7);
            }
        }
        return false;
    }

    private boolean isMainNpcOfPlayer(Player player, int npcId) {
        return (player.gender == ConstPlayer.TRAI_DAT && npcId == ConstNpc.QUY_LAO_KAME)
                || (player.gender == ConstPlayer.NAMEC && npcId == ConstNpc.TRUONG_LAO_GURU)
                || (player.gender == ConstPlayer.XAYDA && npcId == ConstNpc.VUA_VEGETA);
    }

    private boolean checkMainNpcTasks(Player player) {
        return doneTask(player, ConstTask.TASK_9_1) || doneTask(player, ConstTask.TASK_10_2)
                || doneTask(player, ConstTask.TASK_11_3) || doneTask(player, ConstTask.TASK_12_2)
                || doneTask(player, ConstTask.TASK_13_1) || doneTask(player, ConstTask.TASK_14_3)
                || doneTask(player, ConstTask.TASK_15_3) || doneTask(player, ConstTask.TASK_16_3)
                || doneTask(player, ConstTask.TASK_13_0) || doneTask(player, ConstTask.TASK_22_2)
                || doneTask(player, ConstTask.TASK_17_1) || doneTask(player, ConstTask.TASK_18_5)
                || doneTask(player, ConstTask.TASK_19_3) || doneTask(player, ConstTask.TASK_20_6)
                || doneTask(player, ConstTask.TASK_21_4);
    }

    public void checkDoneTaskJoinClan(Player player) {
        if (!player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_13_0);
        }
    }

    public void checkDoneTaskGetItemBox(Player player) {
        if (!player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_0_3);
        }
    }

    public void checkDoneTaskPower(Player player, long power) {
        if (player.isBoss || player.isPet) {
            return;
        }

        if (power >= 16_000) {
            doneTask(player, ConstTask.TASK_7_0);
        }
        if (power >= 40_000) {
            doneTask(player, ConstTask.TASK_8_0);
        }
        if (power >= 200_000) {
            doneTask(player, ConstTask.TASK_10_0);
        }
        if (power >= 500_000) {
            doneTask(player, ConstTask.TASK_11_0);
        }
        if (power >= 550_000) {
            doneTask(player, ConstTask.TASK_11_1);
        }
        if (power >= 600_000) {
            doneTask(player, ConstTask.TASK_11_2);
        }
        if (power >= 600_000_000) {
            doneTask(player, ConstTask.TASK_20_0);
        }
        if (power >= 2_000_000_000L) {
            doneTask(player, ConstTask.TASK_21_0);
        }
    }

    public void checkDoneTaskUseTiemNang(Player player) {
        if (!player.isBoss && !player.isPet) {
            doneTask(player, ConstTask.TASK_3_0);
        }
    }

    public void checkDoneTaskGoToMap(Player player, Zone zoneJoin) {
        if (!player.isPl()) {
            return;
        }

        switch (zoneJoin.map.mapId) {
            case ConstMap.VACH_NUI_ARU, ConstMap.VACH_NUI_MOORI, ConstMap.VUC_PLANT -> {
                if (player.location.x >= 635) {
                    doneTask(player, ConstTask.TASK_0_0);
                }
            }
            case ConstMap.NHA_GOHAN, ConstMap.NHA_MOORI, ConstMap.NHA_BROLY -> {
                doneTask(player, ConstTask.TASK_0_1);
                doneTask(player, ConstTask.TASK_12_0);
            }
            case ConstMap.LANG_ARU, ConstMap.LANG_MORI, ConstMap.LANG_KAKAROT ->
                doneTask(player, ConstTask.TASK_8_0);
            case ConstMap.DAO_KAME, ConstMap.DAO_GURU, ConstMap.VACH_NUI_DEN ->
                doneTask(player, ConstTask.TASK_9_0);
            case ConstMap.THANH_PHO_VEGETA ->
                doneTask(player, ConstTask.TASK_17_0);
            case ConstMap.THANH_PHO_PHIA_NAM ->
                doneTask(player, ConstTask.TASK_23_0);
            case ConstMap.SAN_SAU_SIEU_THI ->
                doneTask(player, ConstTask.TASK_24_0);
            case ConstMap.THANH_PHO_PHIA_BAC ->
                doneTask(player, ConstTask.TASK_25_0);
            case ConstMap.THI_TRAN_GINDER ->
                doneTask(player, ConstTask.TASK_26_0);
            case ConstMap.VO_DAI_XEN_BO_HUNG ->
                doneTask(player, ConstTask.TASK_27_2);
        }
    }

    public void checkDoneTaskUseItem(Player player, Item item) {
        if (player.isBoss || player.isPet || item == null || item.template == null) {
            return;
        }

        switch (item.template.id) {
            // Ví dụ: Nếu dùng vật phẩm ID 74 (Quả táo) thì xong nhiệm vụ X
            // case 74 -> doneTask(player, ConstTask.TASK_X_X);

            // có thể thêm các case nhiệm vụ dùng vật phẩm tại đây sau này
        }
    }

    public void checkDoneTaskPickItem(Player player, ItemMap item) {
        if (player.isBoss || player.isPet || item == null || item.itemTemplate == null) {
            return;
        }

        switch (item.itemTemplate.id) {
            case ITEM_CHICKEN_LEG ->
                doneTask(player, ConstTask.TASK_2_0);
            case ITEM_BABY_MABU -> {
                doneTask(player, ConstTask.TASK_3_1);
                Service.gI().sendFlagBag(player);
            }
            case ITEM_RADAR -> {
                doneTask(player, ConstTask.TASK_27_1);
                Service.gI().sendFlagBag(player);
            }
            case ITEM_MAGIC_PEA_BOX ->
                AchievementService.gI().checkDoneTask(player, ConstAchievement.TRUM_NHAT_NGOC);
        }
    }

    public void checkDoneTaskFind7Stars(Player player) {
        doneTask(player, ConstTask.TASK_8_1);
    }

    public void checkDoneTaskNangCS(Player player) {
        if (!player.isBoss && !player.isPet && player.nPoint.dameg >= 35000) {
            doneTask(player, ConstTask.TASK_27_0);
        }
    }

    public void checkDoneTaskConfirmMenuNpc(Player player, Npc npc, byte select) {
        if (player.isBoss || player.isPet) {
            return;
        }

        if (npc.tempId == ConstNpc.DAU_THAN && select == 0) {
            int menuIndex = player.idMark.getIndexMenu();
            if (menuIndex == ConstNpc.MAGIC_TREE_NON_UPGRADE_LEFT_PEA || menuIndex == ConstNpc.MAGIC_TREE_NON_UPGRADE_FULL_PEA) {
                doneTask(player, ConstTask.TASK_0_4);
            }
        }
    }

    public void checkDoneTaskKillBoss(Player player, Boss boss) {
        if (player == null || player.isBoss || player.isPet) {
            return;
        }

        AchievementService.gI().checkDoneTask(player, ConstAchievement.TRUM_KET_LIEU_BOSS);

        switch ((int) boss.id) {
            case BossID.KUKU ->
                doneTask(player, ConstTask.TASK_19_0);
            case BossID.MAP_DAU_DINH ->
                doneTask(player, ConstTask.TASK_19_1);
            case BossID.RAMBO ->
                doneTask(player, ConstTask.TASK_19_2);
            case BossID.SO_4 ->
                doneTask(player, ConstTask.TASK_20_1);
            case BossID.SO_3 ->
                doneTask(player, ConstTask.TASK_20_2);
            case BossID.SO_2 ->
                doneTask(player, ConstTask.TASK_20_3);
            case BossID.SO_1 ->
                doneTask(player, ConstTask.TASK_20_4);
            case BossID.TIEU_DOI_TRUONG ->
                doneTask(player, ConstTask.TASK_20_5);
            case BossID.FIDE -> {
                switch (boss.currentLevel) {
                    case 0 ->
                        doneTask(player, ConstTask.TASK_21_1);
                    case 1 ->
                        doneTask(player, ConstTask.TASK_21_2);
                    case 2 ->
                        doneTask(player, ConstTask.TASK_21_3);
                }
            }
            case BossID.ANDROID_19 ->
                doneTask(player, ConstTask.TASK_23_1);
            case BossID.DR_KORE ->
                doneTask(player, ConstTask.TASK_23_2);
            case BossID.POC ->
                doneTask(player, ConstTask.TASK_25_1);
            case BossID.PIC ->
                doneTask(player, ConstTask.TASK_25_2);
            case BossID.KING_KONG ->
                doneTask(player, ConstTask.TASK_25_3);
            case BossID.ANDROID_13 ->
                doneTask(player, ConstTask.TASK_24_3);
            case BossID.ANDROID_14 ->
                doneTask(player, ConstTask.TASK_24_2);
            case BossID.ANDROID_15 ->
                doneTask(player, ConstTask.TASK_24_1);
            case BossID.XEN_BO_HUNG -> {
                switch (boss.currentLevel) {
                    case 0 ->
                        doneTask(player, ConstTask.TASK_26_1);
                    case 1 ->
                        doneTask(player, ConstTask.TASK_26_2);
                    case 2 ->
                        doneTask(player, ConstTask.TASK_26_3);
                }
            }
            case BossID.XEN_CON_1, BossID.XEN_CON_2, BossID.XEN_CON_3, BossID.XEN_CON_4, BossID.XEN_CON_5, BossID.XEN_CON_6, BossID.XEN_CON_7 ->
                doneTask(player, ConstTask.TASK_27_3);
            case BossID.SIEU_BO_HUNG -> {
                if (boss.currentLevel == 1) {
                    doneTask(player, ConstTask.TASK_27_4);
                }
            }
            case BossID.DRABURA, BossID.DRABURA_2, BossID.DRABURA_3 -> {
                doneTask(player, ConstTask.TASK_28_1);
                doneTask(player, ConstTask.TASK_28_5);
            }
            case BossID.BUI_BUI, BossID.BUI_BUI_2 -> {
                doneTask(player, ConstTask.TASK_28_2);
                doneTask(player, ConstTask.TASK_28_3);
            }
            case BossID.YA_CON ->
                doneTask(player, ConstTask.TASK_28_4);
            case BossID.MABU_12H ->
                doneTask(player, ConstTask.TASK_28_6);

        }
    }

    public void checkDoneTaskKillMob(Player player, Mob mob) {
        if (player.isBoss || player.isPet) {
            return;
        }

        switch (mob.tempId) {
            case ConstMob.MOC_NHAN ->
                doneTask(player, ConstTask.TASK_1_0);
            case ConstMob.KHUNG_LONG_ME -> {
                doneTask(player, ConstTask.TASK_4_0);
                doneTask(player, ConstTask.TASK_5_1);
                doneTask(player, ConstTask.TASK_6_1);
            }
            case ConstMob.LON_LOI_ME -> {
                doneTask(player, ConstTask.TASK_4_1);
                doneTask(player, ConstTask.TASK_5_0);
                doneTask(player, ConstTask.TASK_6_2);
            }
            case ConstMob.QUY_DAT_ME -> {
                doneTask(player, ConstTask.TASK_4_2);
                doneTask(player, ConstTask.TASK_5_2);
                doneTask(player, ConstTask.TASK_6_0);
            }
            case ConstMob.THAN_LAN_BAY -> {
                if (player.gender == ConstPlayer.TRAI_DAT) {
                    doneTask(player, ConstTask.TASK_7_1);
                }
            }
            case ConstMob.PHI_LONG -> {
                if (player.gender == ConstPlayer.NAMEC) {
                    doneTask(player, ConstTask.TASK_7_1);
                }
            }
            case ConstMob.QUY_BAY -> {
                if (player.gender == ConstPlayer.XAYDA) {
                    doneTask(player, ConstTask.TASK_7_1);
                }
            }
            case ConstMob.OC_MUON_HON, ConstMob.OC_SEN, ConstMob.HEO_XAYDA_ME ->
                doneTask(player, ConstTask.TASK_10_1);
            case ConstMob.HEO_RUNG, ConstMob.HEO_DA_XANH, ConstMob.HEO_XAYDA -> {
                int addAmount = (getClanMemberCountInMap(player) >= NMEMBER_DO_TASK_TOGETHER) ? 2 : 1;
                switch (mob.tempId) {
                    case ConstMob.HEO_RUNG ->
                        doneTask(player, ConstTask.TASK_14_0, addAmount);
                    case ConstMob.HEO_DA_XANH ->
                        doneTask(player, ConstTask.TASK_14_1, addAmount);
                    case ConstMob.HEO_XAYDA ->
                        doneTask(player, ConstTask.TASK_14_2, addAmount);
                }
            }
            case ConstMob.BULON, ConstMob.UKULELE, ConstMob.QUY_MAP -> {
                int addAmount = (getClanMemberCountInMap(player) >= NMEMBER_DO_TASK_TOGETHER) ? 2 : 1;
                switch (mob.tempId) {
                    case ConstMob.BULON ->
                        doneTask(player, ConstTask.TASK_15_0, addAmount);
                    case ConstMob.UKULELE ->
                        doneTask(player, ConstTask.TASK_15_1, addAmount);
                    case ConstMob.QUY_MAP ->
                        doneTask(player, ConstTask.TASK_15_2, addAmount);
                }
            }
            case ConstMob.TAMBOURINE ->
                doneTask(player, ConstTask.TASK_16_0);
            case ConstMob.DRUM ->
                doneTask(player, ConstTask.TASK_16_1);
            case ConstMob.AKKUMAN ->
                doneTask(player, ConstTask.TASK_16_2);
            case ConstMob.NAPPA ->
                doneTask(player, ConstTask.TASK_18_0);
            case ConstMob.SOLDIER ->
                doneTask(player, ConstTask.TASK_18_1);
            case ConstMob.APPULE ->
                doneTask(player, ConstTask.TASK_18_2);
            case ConstMob.RASPBERRY ->
                doneTask(player, ConstTask.TASK_18_3);
            case ConstMob.THAN_LAN_XANH ->
                doneTask(player, ConstTask.TASK_18_4);
            case ConstMob.XEN_CON_CAP_1 ->
                doneTask(player, ConstTask.TASK_22_4);
            case ConstMob.XEN_CON_CAP_3 ->
                doneTask(player, ConstTask.TASK_23_3);
            case ConstMob.XEN_CON_CAP_5 ->
                doneTask(player, ConstTask.TASK_25_4);
            case ConstMob.XEN_CON_CAP_8 ->
                doneTask(player, ConstTask.TASK_26_4);
        }
    }

    private int getClanMemberCountInMap(Player player) {
        if (player.clan == null) {
            return 0;
        }
        int count = 0;
        for (Player pl : player.zone.getPlayers()) {
            if (pl != null && pl.isPl() && pl.clan != null && pl.clan.equals(player.clan)) {
                count++;
            }
        }
        return count;
    }

    private boolean doneTask(Player player, int idTaskCustom) {
        return doneTask(player, idTaskCustom, 1);
    }

    private boolean doneTask(Player player, int idTaskCustom, int countToAdd) {
        if (isCurrentTask(player, idTaskCustom)) {
            addDoneSubTask(player, countToAdd);
            handleTaskDialogues(player, idTaskCustom);
            InventoryService.gI().sendItemBags(player);
            return true;
        }
        return false;
    }

    private void handleTaskDialogues(Player player, int idTaskCustom) {
        switch (idTaskCustom) {
            case ConstTask.TASK_0_0 ->
                NpcService.gI().createTutorial(player, -1, transformName(player, "Làm tốt lắm..\nBây giờ bạn hãy vào nhà ông %2 bên phải để nhận nhiệm vụ mới nhé"));
            case ConstTask.TASK_0_1 ->
                NpcService.gI().createTutorial(player, -1, transformName(player, "Ông %2 đang đứng đợi kìa\nHãy nhấn 2 lần vào để nói chuyện"));
            case ConstTask.TASK_0_2 ->
                npcSay(player, ConstTask.NPC_NHA, "Con vừa đi đâu về đó?\nCon hãy đến rương đồ để lấy rađa..\n..sau đó thu hoạch hết đậu trên cây đậu thần đằng kia!");
            case ConstTask.TASK_0_5 ->
                npcSay(player, ConstTask.NPC_NHA, "Tốt lắm, rađa sẽ giúp con thấy được lượng máu và thể lực ở bên góc trái\nBây giờ con hãy đi luyện tập\nCon hãy ra %1, ở đó có những con mộc nhân cho con luyện tập dó\nHãy đốn ngã 5 con mộc nhân cho ông");
            case ConstTask.TASK_1_0, ConstTask.TASK_4_0, ConstTask.TASK_5_1, ConstTask.TASK_6_1, ConstTask.TASK_4_1, ConstTask.TASK_5_0, ConstTask.TASK_6_2, ConstTask.TASK_4_2, ConstTask.TASK_5_2, ConstTask.TASK_6_0 -> {
                if (isCurrentTask(player, idTaskCustom)) {
                    String mobName = getMobNameForTask(idTaskCustom);
                    Service.gI().sendThongBao(player, "Bạn đánh được " + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/" + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount + " " + mobName);
                }
            }
            case ConstTask.TASK_1_1 ->
                npcSay(player, ConstTask.NPC_NHA, "Thể lực của con cũng khá tốt\nCon à, dạo gần đây dân làng của chúng ta gặp phải vài chuyện\nBên cạnh làng ta đột nhiên xuất hiện lũ quái vật\nNó tàn sát dân làng và phá hoại nông sản làng ta\nCon hãy tìm đánh chúng và đem về đây 10 cái đùi gà, 2 ông cháu mình sẽ để dành ăn dần\nĐây là tấm bản đồ của vùng này, con hãy xem để tìm đến %3\nCon có thể sử dụng đậu thần khi hết HP hoặc KI, bằng cách nhấn vào nút có hình trái tim bên góc phải dưới màn hình\nNhanh lên, ông đói lắm rồi");
            case ConstTask.TASK_2_1 -> {
                try {
                    InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, ITEM_CHICKEN_LEG), 10);
                } catch (Exception ignored) {
                }
                Service.gI().dropItemMapForMe(player, player.zone.getItemMapByTempId(ITEM_APPLE));
                npcSay(player, ConstTask.NPC_NHA, "Tốt lắm, đùi gà đây rồi, haha. Ông sẽ nướng tại đống lửa gần kia con có thể ăn bất cứ lúc nào nếu muốn\nÀ cháu này, vừa nãy ông có nghe thấy 1 tiếng động lớn, hình như có 1 vật thể rơi tại %5, con hãy đến kiểm tra xem\nCon cũng có thể dùng tiềm năng bản thân để nâng HP, KI hoặc sức đánh");
            }
            case ConstTask.TASK_3_2 -> {
                try {
                    InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, ITEM_BABY_MABU), 1);
                } catch (Exception ignored) {
                }
                Service.gI().sendFlagBag(player);
                npcSay(player, ConstTask.NPC_NHA, "Có em bé trong phi thuyền rơi xuống à, ông cứ tưởng là sao băng chứ\nÔng sẽ đặt tên cho em nó là Goku, từ giờ nó sẽ là thành viên trong gia đình ta\nNãy ông mới nhận được tin có bầy mãnh thú xuất hiện tại Trạm phi thuyền\nBọn chúng vừa đổ bộ xuống trái đất để trả thù việc con sát hại con chúng\nCon hãy đi tiêu diệt chúng để giúp dân làng tại đó luôn nhé");
            }
            case ConstTask.TASK_4_3, ConstTask.TASK_5_3, ConstTask.TASK_6_3 ->
                npcSay(player, ConstTask.NPC_NHA, "Ông rất tự hào về con\nÔng cho con cuốn bí kíp này để nâng cao võ học\nHãy dùng sức mạnh của mình trừ gian diệt ác bảo vệ dân lành con nhé\nBây giờ con hãy đi tập luyện đi, khi nào mạnh hơn thì quay về đây ông giao cho nhiệm vụ mới\nĐi đi..");
            case ConstTask.TASK_7_2 -> {
                Item capsule = ItemService.gI().createNewItem((short) 193, 75);
                InventoryService.gI().addItemBag(player, capsule);
                npcSay(player, ConstTask.NPC_SHOP_LANG, "Hiện tại em vẫn khỏe anh ạ, hơi bị trầy xước tí thôi nhưng không sao\nEm thực sự cảm ơn anh đã cứu em, nếu không có anh thì giờ này cũng không biết em sẽ thế nào nữa\nÀ em có cái món này, tuy nó không quá giá trị nhưng em mong anh nhận cho em vui");
            }
            case ConstTask.TASK_7_3 ->
                npcSay(player, ConstTask.NPC_NHA, "Ôi bạn ơi, sức đề kháng bạn yếu là do bạn chưa chơi đồ đấy bạn ạ");
            case ConstTask.TASK_8_2 ->
                npcSay(player, ConstTask.NPC_NHA, "Cháu trai của ông, con làm ông tự hào lắm. Con đã biết dùng sức mạnh của mình để giúp kẻ yếu\nBây giờ con đã trưởng thành thực sự rồi, ông sẽ bàn giao con lại cho %10 - người bạn lâu ngày không gặp của ông\nCon hãy tìm đường tới %11 và gửi lời chào của ông tới lão ấy nhé\nĐi đi con...");
            case ConstTask.TASK_9_1 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Chào cậu bé, cháu có phải cháu nội ông %2 phải không?\nTa cũng đã gặp cháu 1 lần hồi cháu còn bé xíu à\nBây giờ cháu muốn ta nhận cháu làm đệ tử à? Ta cũng không biết thực lực của cháu hiện tại như nào nữa\nCháu bé hãy đi đánh mấy con %12 ở quanh đây thể hiện tài năng và ta sẽ coi như đó là học phí nhé");
            case ConstTask.TASK_10_2 -> {
                Item skill2 = ItemService.gI().createNewItem((short) (player.gender == 0 ? 94 : player.gender == 1 ? 101 : 108), 1);
                InventoryService.gI().addItemBag(player, skill2);
                npcSay(player, ConstTask.NPC_QUY_LAO, "Tốt lắm, bây giờ con đã chính thức trở thành đệ tử của ta\nTa sẽ dạy con 1 tuyệt chiêu đặc biệt của ta\nBây giờ con hãy đi kết bạn với những người xung quanh đây đi, thêm 1 người bạn bớt 1 kẻ thù mà con\nMà lưu ý là tránh kết bạn với những người có bang hội nhé, họ không là kẻ thù cũng không nên là bạn");
            }
            case ConstTask.TASK_11_3 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Giờ đây xã giao của con đã tiến bộ hơn rất nhiều rồi\nBây giờ con hãy về nhà xin ông %2 rằng con sẽ vào bang hội nhé\nTa sợ lão ấy không đồng ý lại quay sang trách móc cái thân già này..\nĐi đi con, nói khéo lão ấy nhé.");
            case ConstTask.TASK_12_1 ->
                npcSay(player, ConstTask.NPC_NHA, "Con muốn tham gia vào bang hội á? Haizz, cái lão già này lại dạy hư cháu ông rồi\nCon muốn thì cũng được thôi, nhưng con phải biết lựa chọn được bang hội nào tốt đấy nhé..\n..xã hội này có nhiều thành phần lắm, cũng chỉ vì an nguy của con nên ông chỉ biết dặn dò vậy\nChúc con may mắn trên con đường con chọn, mà luôn nhớ rằng con phải là 1 công dân tốt đấy nhé..");
            case ConstTask.TASK_12_2 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Cuối cùng lão ấy cũng đồng ý rồi à? Tốt lắm\nBây giờ con hãy cùng những người bạn con vừa kết bạn tạo thành 1 bang hội đi nhé\nKhi nào đủ 5 thành viên bang hãy tới đây ta giao nhiệm vụ cho tất cả các con");
            case ConstTask.TASK_13_1 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Tốt lắm, con đã có những người đồng đội kề vai sát cánh rồi\nBây giờ con và 3 người họ hãy thể hiện tinh thần đoàn kết đi nào\nCách phối hợp nhau làm nhiệm vụ, cách cư xử với nhau đó là hiện thân của tâm tính mỗi người\nCác con hãy đối nhân xử thế với nhau, hãy cùng hợp sức tiêu diệt lũ quái vật nhé");
            case ConstTask.TASK_14_3 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Giỏi lắm các con!\n...Hiện tại có vài chủng quái vật mới đổ bộ lên hành tinh chúng ta\nCon hãy cùng 3 người trong bang lên đường tiêu diệt chúng nhé\nDân chúng đặt niềm tin vào các con hết đấy..\nĐi đi...");
            case ConstTask.TASK_15_3 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Giỏi lắm các con\nCòn 1 vài con quái vật đầu sỏ nữa\nCon hãy tiêu diệt nốt chúng đi nhé..");
            case ConstTask.TASK_16_3 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Con thực sự làm ta ngạc nhiên đấy, không uổng công ta truyền dạy võ công\nBên ngoài còn rất nhiều kẻ thù nguy hiểm, nên con phải không ngừng luyện tập nhé\nLại có chuyện xảy ra rồi, Cui - một người họ hàng xa của họ hàng ta - đang gặp chuyện\nCon hãy tới thành phố Vegeta hỏi thăm tình hình cậu ta nhé! Đi đi con..");
            case ConstTask.TASK_17_1 ->
                npcSay(player, ConstNpc.CUI, "Chào cậu, cậu là đệ tử của %10 phải không\nBọn người ngoài hành tinh cầm đầu bởi tên Fide đã và đang đổ bộ vào quê hương của tôi..\n..chúng tàn sát hết dân lành và hủy hoại quê hương chúng tôi\nCậu hãy giúp tôi 1 tay tiêu diệt bọn chúng nhé");
            case ConstTask.TASK_18_5 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Cảm ơn cậu đã hỗ trợ tôi tiêu diệt bọn lính tay sai Fide\n3 tên cầm đầu chúng đang tức giận lắm, tôi thì không đủ mạnh để chống lại bọn chúng\n...");
            case ConstTask.TASK_19_3 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "Cảm ơn cậu đã tiêu diệt giúp tôi lũ đệ tử của Fide\nDưới trướng Fide còn có 1 đội gồm 5 thành viên được chúng gọi là Tiều Đội Sát Thủ\nChúng rất mạnh và rất trung thành với tên Fide\nBọn chúng vừa được cử tới đi trả thù cho 3 tên đệ tử cậu vừa tiêu diệt\nHãy chống lại bọn chúng giúp tôi nhé....");
            case ConstTask.TASK_20_6 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "TanTaiPro");
            case ConstTask.TASK_21_4 ->
                npcSay(player, ConstTask.NPC_QUY_LAO, "TanTaiPro\nTanTaiPro\nTanTaiPro");
            case ConstTask.TASK_22_0 ->
                npcSay(player, ConstTask.NPC_NHA, "Ngon");
            case ConstTask.TASK_22_1 ->
                npcSay(player, ConstNpc.CALICK, ConstNpc.CALICK_KE_CHUYEN);
            case ConstTask.TASK_22_2 ->
                npcSay(player, ConstNpc.QUY_LAO_KAME, "I a cờ bú");
            case ConstTask.TASK_22_3 ->
                npcSay(player, ConstNpc.BUNMA_TL, "Mau đi tiêu diệt 1000 xên cấp 1 đi em");
            case ConstTask.TASK_22_5 ->
                npcSay(player, ConstNpc.BUNMA_TL, "Đến Thành Phố Phía Đông tiêu diệt Tokuda à nhầm Dr.Kore và đàn em của hắn.");
            case ConstTask.TASK_23_4 ->
                npcSay(player, ConstNpc.BUNMA_TL, "Bọn Android đã xuất hiện tại sân sau siêu thị mau đi trừ khử chúng");
            case ConstTask.TASK_24_4 ->
                npcSay(player, ConstNpc.BUNMA_TL, "Quá ghê gớm =))");
            case ConstTask.TASK_25_5 ->
                npcSay(player, ConstNpc.BUNMA_TL, "Cũng ra gì đấy! Khét đấy nhề!");
            case ConstTask.TASK_26_5 ->
                npcSay(player, ConstNpc.BUNMA_TL, "Hãy đến võ đài xên bọ hung và tiêu diệt 7 đứa con của nó");
            case ConstTask.TASK_27_0 ->
                npcSay(player, ConstNpc.THAN_MEO_KARIN, "Tốt lắm! Bây giờ con hãy tìm cho ta 500 viên capsulue kì bí");
            case ConstTask.TASK_27_5 ->
                npcSay(player, ConstNpc.BUNMA_TL, "Vào lúc 12h trưa các ngày, bạn đến gặp NPC Ô sin tại map Đại hội võ thuật. sau đó bạn đến các tầng của map để tiêu diệt các mục tiêu:\nHạ 25 Drabura\nHạ 25 Bui Bui\nHạ 25 Bui Bui lần 2\nHạ 25 Yacon\nHạ 25 Drabura lần 2\nHạ 50 Mabư");
        }
    }

    private String getMobNameForTask(int taskId) {
        return switch (taskId) {
            case ConstTask.TASK_1_0 ->
                "mộc nhân";
            case ConstTask.TASK_4_0, ConstTask.TASK_5_1, ConstTask.TASK_6_1 ->
                "khủng long mẹ";
            case ConstTask.TASK_4_1, ConstTask.TASK_5_0, ConstTask.TASK_6_2 ->
                "lợn lòi mẹ";
            case ConstTask.TASK_4_2, ConstTask.TASK_5_2, ConstTask.TASK_6_0 ->
                "quỷ đất mẹ";
            default ->
                "quái vật";
        };
    }

    private void npcSay(Player player, int npcId, String text) {
        npcId = transformNpcId(player, npcId);
        text = transformName(player, text);
        int avatar = NpcService.gI().getAvatar(npcId);
        NpcService.gI().createTutorial(player, avatar, text);
    }

    private void rewardDoneTask(Player player) {
        int taskId = player.playerTask.taskMain.id;

        if (taskId >= 0 && taskId < TASK_BASE_REWARDS.length) {
            int rewardValue = TASK_BASE_REWARDS[taskId];
            Service.gI().addSMTN(player, (byte) 0, rewardValue, false);
            Service.gI().addSMTN(player, (byte) 1, rewardValue, false);
        }

        if (taskId >= 0 && taskId < 25) {
            long bonusSMTN = 500L * (taskId + 1);
            Service.gI().addSMTN(player, (byte) 2, bonusSMTN, false);

            long bonusGold = (taskId < 5) ? 100_000L * (taskId + 1) : 500_000L;
            player.inventory.addGold((int) bonusGold);
            Service.gI().sendMoney(player);
        }
    }

    private void addDoneSubTask(Player player, int numDone) {
        player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count += numDone;
        if (player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count >= player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount) {
            player.playerTask.taskMain.index++;
            if (player.playerTask.taskMain.index >= player.playerTask.taskMain.subTasks.size()) {
                this.sendNextTaskMain(player);
            } else {
                this.sendNextSubTask(player);
            }
        } else {
            this.sendUpdateCountSubTask(player);
        }
    }

    private int transformMapId(Player player, int id) {
        if (id == ConstTask.MAP_NHA) {
            return player.gender + 21;
        }
        if (id == ConstTask.MAP_200) {
            return player.gender == ConstPlayer.TRAI_DAT ? 1 : (player.gender == ConstPlayer.NAMEC ? 8 : 15);
        }
        if (id == ConstTask.MAP_VACH_NUI) {
            return player.gender == ConstPlayer.TRAI_DAT ? 39 : (player.gender == ConstPlayer.NAMEC ? 40 : 41);
        }
        if (id == ConstTask.MAP_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT ? 24 : (player.gender == ConstPlayer.NAMEC ? 25 : 26);
        }
        if (id == ConstTask.MAP_QUAI_BAY_600) {
            return player.gender == ConstPlayer.TRAI_DAT ? 3 : (player.gender == ConstPlayer.NAMEC ? 11 : 17);
        }
        if (id == ConstTask.MAP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT ? 0 : (player.gender == ConstPlayer.NAMEC ? 7 : 14);
        }
        if (id == ConstTask.MAP_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT ? 5 : (player.gender == ConstPlayer.NAMEC ? 13 : 20);
        }
        return id;
    }

    private int transformNpcId(Player player, int id) {
        if (id == ConstTask.NPC_NHA) {
            return player.gender == ConstPlayer.TRAI_DAT ? ConstNpc.ONG_GOHAN : (player.gender == ConstPlayer.NAMEC ? ConstNpc.ONG_MOORI : ConstNpc.ONG_PARAGUS);
        }
        if (id == ConstTask.NPC_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT ? ConstNpc.DR_DRIEF : (player.gender == ConstPlayer.NAMEC ? ConstNpc.CARGO : ConstNpc.CUI);
        }
        if (id == ConstTask.NPC_SHOP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT ? ConstNpc.BUNMA : (player.gender == ConstPlayer.NAMEC ? ConstNpc.DENDE : ConstNpc.APPULE);
        }
        if (id == ConstTask.NPC_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT ? ConstNpc.QUY_LAO_KAME : (player.gender == ConstPlayer.NAMEC ? ConstNpc.TRUONG_LAO_GURU : ConstNpc.VUA_VEGETA);
        }
        return id;
    }

    private String transformName(Player player, String text) {
        String quai1000 = player.gender == ConstPlayer.XAYDA ? "thằn lằn mẹ" : (player.gender == ConstPlayer.TRAI_DAT ? "phi long mẹ" : "quỷ bay mẹ");
        String map600 = player.gender == ConstPlayer.TRAI_DAT ? "Rừng nấm" : (player.gender == ConstPlayer.NAMEC ? "Thung lũng Namếc" : "Rừng nguyên sinh");
        String npcQuyLao = player.gender == ConstPlayer.TRAI_DAT ? "Quy Lão Kame" : (player.gender == ConstPlayer.NAMEC ? "Trưởng lão Guru" : "Vua Vegeta");
        String mapQuyLao = player.gender == ConstPlayer.TRAI_DAT ? "Đảo Kamê" : (player.gender == ConstPlayer.NAMEC ? "Đảo Guru" : "Vách núi đen");
        String quai3000 = player.gender == ConstPlayer.TRAI_DAT ? "ốc mượn hồn" : (player.gender == ConstPlayer.NAMEC ? "ốc sên" : "heo Xayda mẹ");
        String lang = player.gender == ConstPlayer.TRAI_DAT ? "Làng Aru" : (player.gender == ConstPlayer.NAMEC ? "Làng Mori" : "Làng Kakarot");
        String npcNha = player.gender == ConstPlayer.TRAI_DAT ? "ông Gôhan" : (player.gender == ConstPlayer.NAMEC ? "ông Moori" : "ông Paragus");
        String quai200 = player.gender == ConstPlayer.TRAI_DAT ? "khủng long" : (player.gender == ConstPlayer.NAMEC ? "lợn lòi" : "quỷ đất");
        String map200 = player.gender == ConstPlayer.TRAI_DAT ? "Đồi hoa cúc" : (player.gender == ConstPlayer.NAMEC ? "Đồi nấm tím" : "Đồi hoang");
        String vachNui = player.gender == ConstPlayer.TRAI_DAT ? "Vách núi Aru" : (player.gender == ConstPlayer.NAMEC ? "Vách núi Moori" : "Vách núi Kakarot");
        String map500 = player.gender == ConstPlayer.TRAI_DAT ? "Thung lũng tre" : (player.gender == ConstPlayer.NAMEC ? "Thị trấn Moori" : "Làng Plant");
        String npcTtvt = player.gender == ConstPlayer.TRAI_DAT ? "Dr. Brief" : (player.gender == ConstPlayer.NAMEC ? "Cargo" : "Cui");
        String quaiBay600 = player.gender == ConstPlayer.TRAI_DAT ? "thằn lằn bay" : (player.gender == ConstPlayer.NAMEC ? "phi long" : "quỷ bay");
        String npcShopLang = player.gender == ConstPlayer.TRAI_DAT ? "Bunma" : (player.gender == ConstPlayer.NAMEC ? "Dende" : "Appule");

        return text.replace(ConstTask.TEN_QUAI_1000, quai1000)
                .replace(ConstTask.TEN_MAP_600, map600)
                .replace(ConstTask.TEN_NPC_QUY_LAO, npcQuyLao)
                .replace(ConstTask.TEN_MAP_QUY_LAO, mapQuyLao)
                .replace(ConstTask.TEN_QUAI_3000, quai3000)
                .replace(ConstTask.TEN_LANG, lang)
                .replace(ConstTask.TEN_NPC_NHA, npcNha)
                .replace(ConstTask.TEN_QUAI_200, quai200)
                .replace(ConstTask.TEN_MAP_200, map200)
                .replace(ConstTask.TEN_VACH_NUI, vachNui)
                .replace(ConstTask.TEN_MAP_500, map500)
                .replace(ConstTask.TEN_NPC_TTVT, npcTtvt)
                .replace(ConstTask.TEN_QUAI_BAY_600, quaiBay600)
                .replace(ConstTask.TEN_NPC_SHOP_LANG, npcShopLang);
    }

    private boolean isCurrentTask(Player player, int idTaskCustom) {
        if (player.playerTask == null || player.playerTask.taskMain == null) {
            return false;
        }
        int currentTaskCode = ((player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index) << 1;
        return idTaskCustom == currentTaskCode;
    }

    public int getIdTask(Player player) {
        if (player.isPet || player.isBoss || player.playerTask == null || player.playerTask.taskMain == null) {
            return -1;
        }
        return ((player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index) << 1;
    }

    public SideTaskTemplate getSideTaskTemplateById(int id) {
        return (id != -1) ? GameData.SIDE_TASKS_TEMPLATE.get(id) : null;
    }

    public void changeSideTask(Player player, byte level) {
        player.playerTask.sideTask.renew();
        if (player.playerTask.sideTask.leftTask > 0) {
            player.playerTask.sideTask.reset();
            SideTaskTemplate temp = GameData.SIDE_TASKS_TEMPLATE.get(Util.nextInt(0, GameData.SIDE_TASKS_TEMPLATE.size() - 1));
            player.playerTask.sideTask.template = temp;
            player.playerTask.sideTask.maxCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
            player.playerTask.sideTask.leftTask--;
            player.playerTask.sideTask.level = level;
            player.playerTask.sideTask.receivedTime = System.currentTimeMillis();
            Service.gI().sendThongBao(player, "Bạn nhận được nhiệm vụ: " + player.playerTask.sideTask.getName());
        } else {
            Service.gI().sendThongBao(player, "Bạn đã nhận hết nhiệm vụ hôm nay. Hãy chờ tới ngày mai rồi nhận tiếp");
        }
    }

    public void removeSideTask(Player player) {
        Service.gI().sendThongBao(player, "Bạn vừa hủy bỏ nhiệm vụ " + player.playerTask.sideTask.getName());
        player.playerTask.sideTask.reset();
    }

    public void paySideTask(Player player) {
        if (player.playerTask.sideTask.template == null || !player.playerTask.sideTask.isDone()) {
            Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang của bạn không đủ chỗ trống để nhận thưởng.");
            return;
        }

        int goldReward = 0;
        int ngocBi = 708;
        int cayThong = -1;

        switch (player.playerTask.sideTask.level) {
            case ConstTask.EASY -> {
                goldReward = ConstTask.GOLD_EASY;
                ngocBi = 708;
            }
            case ConstTask.NORMAL -> {
                goldReward = ConstTask.GOLD_NORMAL;
                ngocBi = 707;
            }
            case ConstTask.HARD -> {
                goldReward = ConstTask.GOLD_HARD;
                ngocBi = 706;
            }
            case ConstTask.VERY_HARD -> {
                goldReward = ConstTask.GOLD_VERY_HARD;
                ngocBi = 705;
            }
            case ConstTask.HELL -> {
                goldReward = ConstTask.GOLD_HELL;
                ngocBi = 704;
                if (player.playerTask.sideTask.leftTask < 15) {
                    cayThong = ITEM_CHRISTMAS_TREE;
                }
            }
        }

        boolean canDropTree = (cayThong != -1) && Util.isTrue(1, 10);

        if (canDropTree) {
            Item cT = ItemService.gI().createNewItem((short) cayThong);
            if (Util.isTrue(1, 2)) {
                cT.itemOptions.add(new Item.ItemOption(Util.nextInt(11, 13), 100));
            }
            cT.itemOptions.add(new Item.ItemOption(24, 0));
            cT.itemOptions.add(new Item.ItemOption(110, Util.nextInt(118, 126)));
            cT.itemOptions.add(new Item.ItemOption(110, Util.nextInt(110, 113)));
            cT.itemOptions.add(new Item.ItemOption(93, 30));
            InventoryService.gI().addItemBag(player, cT);
            Service.gI().sendThongBao(player, "Bạn nhận được " + cT.template.name);
        } else {
            Item bi = ItemService.gI().createNewItem((short) ngocBi);
            bi.itemOptions.add(new Item.ItemOption(93, 30));
            InventoryService.gI().addItemBag(player, bi);
            Service.gI().sendThongBao(player, "Bạn nhận được " + bi.template.name);
        }

        InventoryService.gI().sendItemBags(player);
        player.inventory.addGold(goldReward);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Bạn nhận được " + Util.numberToMoney(goldReward) + " vàng");
        player.playerTask.sideTask.reset();
    }

    public void checkDoneSideTaskKillMob(Player player, Mob mob) {
        if (player.playerTask == null || player.playerTask.sideTask.template == null) {
            return;
        }
        int taskId = player.playerTask.sideTask.template.id;

        if (taskId >= 0 && taskId < TASK_MOB_MAPPING.length && mob.tempId == TASK_MOB_MAPPING[taskId]) {
            player.playerTask.sideTask.count++;
            notifyProcessTask(player, true);
        }
    }

    public void checkDoneSideTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask != null && player.playerTask.sideTask != null && player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.template.id == 58 && item.itemTemplate.type == 9) {
                player.playerTask.sideTask.count += item.quantity;
                notifyProcessTask(player, true);
            }
        }
    }

    public ClanTaskTemplate getClanTaskTemplateById(int id) {
        return (id != -1) ? GameData.CLAN_TASKS_TEMPLATE.get(id) : null;
    }

    public void changeClanTask(Npc npc, Player player, byte level) {
        player.playerTask.clanTask.renew();
        if (player.playerTask.clanTask.leftTask > 0) {
            player.playerTask.clanTask.reset();
            ClanTaskTemplate temp = GameData.CLAN_TASKS_TEMPLATE.get(Util.nextInt(0, GameData.CLAN_TASKS_TEMPLATE.size() - 1));
            player.playerTask.clanTask.template = temp;
            player.playerTask.clanTask.maxCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
            player.playerTask.clanTask.level = level;
            player.playerTask.clanTask.receivedTime = System.currentTimeMillis();
            npc.createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
        } else {
            npc.createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Đã hết nhiệm vụ cho hôm nay, hãy chờ đến ngày mai", "OK", "Từ chối");
        }
    }

    public void removeClanTask(Player player) {
        Service.gI().sendThongBao(player, "Đã hủy nhiệm vụ bang.");
        player.playerTask.clanTask.leftTask--;
        player.playerTask.clanTask.reset();
    }

    public void payClanTask(Player player) {
        if (player.playerTask.clanTask.template == null || !player.playerTask.clanTask.isDone()) {
            Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            return;
        }

        int capsuleClan = (player.playerTask.clanTask.level + 1) * 10;
        player.playerTask.clanTask.leftTask--;
        player.playerTask.clanTask.reset();
        Service.gI().sendThongBao(player, "Bạn vừa nhận được " + Util.numberToMoney(capsuleClan) + " capsule bang.");

        if (player.clan != null) {
            player.clan.capsuleClan += capsuleClan;
            for (ClanMember cm : player.clan.getMembers()) {
                if (cm.id == player.id) {
                    cm.memberPoint += capsuleClan;
                    cm.clanPoint += capsuleClan;
                    break;
                }
            }
            for (ClanMember cm : player.clan.getMembers()) {
                Player pl = Client.gI().getPlayer(cm.id);
                if (pl != null) {
                    ClanService.gI().sendMyClan(player);
                }
            }
        }
    }

    public void checkDoneClanTaskKillMob(Player player, Mob mob) {
        if (player.playerTask == null || player.playerTask.clanTask.template == null) {
            return;
        }
        int taskId = player.playerTask.clanTask.template.id;

        if (taskId >= 0 && taskId < TASK_MOB_MAPPING.length && mob.tempId == TASK_MOB_MAPPING[taskId]) {
            player.playerTask.clanTask.count++;
            notifyProcessTask(player, false);
        }
    }

    public void checkDoneClanTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask != null && player.playerTask.clanTask != null && player.playerTask.clanTask.template != null && item != null && item.itemTemplate != null) {
            if (player.playerTask.clanTask.template.id == 58 && item.itemTemplate.type == 9) {
                player.playerTask.clanTask.count += item.quantity;
                notifyProcessTask(player, false);
            }
        }
    }

    private void notifyProcessTask(Player player, boolean isSideTask) {
        int percentDone = isSideTask ? player.playerTask.sideTask.getPercentProcess() : player.playerTask.clanTask.getPercentProcess();

        if (percentDone == 100) {
            String msg = isSideTask ? "Chúc mừng bạn đã hoàn thành nhiệm vụ, bây giờ hãy quay về Bò Mộng trả nhiệm vụ." : "Tiếp theo hãy về Bang hội báo cáo.";
            Service.gI().sendThongBao(player, msg);
            return;
        }

        boolean shouldNotify = false;
        int threshold = (percentDone / 10) * 10;

        if (isSideTask) {
            switch (threshold) {
                case 90 -> {
                    if (!player.playerTask.sideTask.notify90) {
                        player.playerTask.sideTask.notify90 = true;
                        shouldNotify = true;
                    }
                }
                case 80 -> {
                    if (!player.playerTask.sideTask.notify80) {
                        player.playerTask.sideTask.notify80 = true;
                        shouldNotify = true;
                    }
                }
                case 70 -> {
                    if (!player.playerTask.sideTask.notify70) {
                        player.playerTask.sideTask.notify70 = true;
                        shouldNotify = true;
                    }
                }
                case 60 -> {
                    if (!player.playerTask.sideTask.notify60) {
                        player.playerTask.sideTask.notify60 = true;
                        shouldNotify = true;
                    }
                }
                case 50 -> {
                    if (!player.playerTask.sideTask.notify50) {
                        player.playerTask.sideTask.notify50 = true;
                        shouldNotify = true;
                    }
                }
                case 40 -> {
                    if (!player.playerTask.sideTask.notify40) {
                        player.playerTask.sideTask.notify40 = true;
                        shouldNotify = true;
                    }
                }
                case 30 -> {
                    if (!player.playerTask.sideTask.notify30) {
                        player.playerTask.sideTask.notify30 = true;
                        shouldNotify = true;
                    }
                }
                case 20 -> {
                    if (!player.playerTask.sideTask.notify20) {
                        player.playerTask.sideTask.notify20 = true;
                        shouldNotify = true;
                    }
                }
                case 10 -> {
                    if (!player.playerTask.sideTask.notify10) {
                        player.playerTask.sideTask.notify10 = true;
                        shouldNotify = true;
                    }
                }
                case 0 -> {
                    if (!player.playerTask.sideTask.notify0) {
                        player.playerTask.sideTask.notify0 = true;
                        shouldNotify = true;
                    }
                }
            }
        } else {
            switch (threshold) {
                case 90 -> {
                    if (!player.playerTask.clanTask.notify90) {
                        player.playerTask.clanTask.notify90 = true;
                        shouldNotify = true;
                    }
                }
                case 80 -> {
                    if (!player.playerTask.clanTask.notify80) {
                        player.playerTask.clanTask.notify80 = true;
                        shouldNotify = true;
                    }
                }
                case 70 -> {
                    if (!player.playerTask.clanTask.notify70) {
                        player.playerTask.clanTask.notify70 = true;
                        shouldNotify = true;
                    }
                }
                case 60 -> {
                    if (!player.playerTask.clanTask.notify60) {
                        player.playerTask.clanTask.notify60 = true;
                        shouldNotify = true;
                    }
                }
                case 50 -> {
                    if (!player.playerTask.clanTask.notify50) {
                        player.playerTask.clanTask.notify50 = true;
                        shouldNotify = true;
                    }
                }
                case 40 -> {
                    if (!player.playerTask.clanTask.notify40) {
                        player.playerTask.clanTask.notify40 = true;
                        shouldNotify = true;
                    }
                }
                case 30 -> {
                    if (!player.playerTask.clanTask.notify30) {
                        player.playerTask.clanTask.notify30 = true;
                        shouldNotify = true;
                    }
                }
                case 20 -> {
                    if (!player.playerTask.clanTask.notify20) {
                        player.playerTask.clanTask.notify20 = true;
                        shouldNotify = true;
                    }
                }
                case 10 -> {
                    if (!player.playerTask.clanTask.notify10) {
                        player.playerTask.clanTask.notify10 = true;
                        shouldNotify = true;
                    }
                }
                case 0 -> {
                    if (!player.playerTask.clanTask.notify0) {
                        player.playerTask.clanTask.notify0 = true;
                        shouldNotify = true;
                    }
                }
            }
        }

        if (shouldNotify) {
            String taskName = isSideTask ? player.playerTask.sideTask.getName() : player.playerTask.clanTask.getName();
            int count = isSideTask ? player.playerTask.sideTask.count : player.playerTask.clanTask.count;
            int maxCount = isSideTask ? player.playerTask.sideTask.maxCount : player.playerTask.clanTask.maxCount;
            Service.gI().sendThongBao(player, "Nhiệm vụ: " + taskName + " đã hoàn thành: " + count + "/" + maxCount + " (" + percentDone + "%)");
        }
    }
}
