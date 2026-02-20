package npc.list;

import services.dungeon.TreasureUnderSeaService;
import dungeon.TreasureUnderSea;
import consts.ConstNpc;
import consts.ConstMap;
import item.Item;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.RewardService;
import services.Service;
import services.TaskService;
import services.func.Input;
import shop.ShopService;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;
import java.util.ArrayList;
import java.util.List;
import static npc.NpcFactory.PLAYERID_OBJECT;
import player.skill.LearnSkillService;

public class QuyLaoKame extends Npc {

    private static final int ID_RUA_CON = 874;
    private static final int MENU_CHOOSE_ACTION = 0;
    private static final int MENU_GIAO_RUA_CON = 1;
    private static final int MENU_XAC_NHAN_HUY_HOC = 13;
    private static final int MENU_XAC_NHAN_GIAI_TAN = 4;
    private static final int TIME_UNIT_FOR_GEM_CALC = 600_000;
    private static final int Y_LANDING_CLAN_AREA = 432;

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player) || TaskService.gI().checkDoneTaskTalkNpc(player, this)) return;

        if (player.canReward) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", "Giao\nLân con");
            return;
        }

        List<String> menu = new ArrayList<>();
        menu.add("Nói\nchuyện");
        
        Item ruaCon = InventoryService.gI().findItemBag(player, ID_RUA_CON);
        if (ruaCon != null && ruaCon.quantity >= 1) {
            menu.add("Giao\nRùa con");
        }

        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menu.toArray(String[]::new));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        if (player.canReward) {
            RewardService.gI().rewardLancon(player);
            return;
        }

        int indexMenu = player.idMark.getIndexMenu();
        switch (indexMenu) {
            case ConstNpc.BASE_MENU -> handleBaseMenuSelect(player, select);
            case MENU_CHOOSE_ACTION -> handleActionMenuSelect(player, select);
            case ConstNpc.MENU_HOC_KY_NANG -> handleHocKyNangMenu(player, select);
            case MENU_XAC_NHAN_HUY_HOC -> handleConfirmHuyHoc(player, select);
            case MENU_XAC_NHAN_GIAI_TAN -> handleConfirmGiaiTan(player, select);
            case ConstNpc.MENU_OPEN_DBKB, ConstNpc.MENU_OPENED_DBKB -> handleBanDoKhoBau(player, select);
            case ConstNpc.MENU_ACCEPT_GO_TO_BDKB -> handleAcceptGoToBDKB(player, select);
        }
    }

    private void handleBaseMenuSelect(Player player, int select) {
        if (select == 0) {
            if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
                LearnSkillService.gI().finishLearnSkill(player);
            }

            List<String> menu = new ArrayList<>();
            menu.add("Nhiệm vụ");
            menu.add("Học\nKỹ năng");
            if (player.clan != null) {
                menu.add("Về khu\nvực bang");
                if (player.clan.isLeader(player)) {
                    menu.add("Giải tán\nBang hội");
                    menu.add("Kho báu\ndưới biển");
                }
            }
            this.createOtherMenu(player, MENU_CHOOSE_ACTION, "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menu.toArray(String[]::new));
        } else if (select == 1) {
            Item ruaCon = InventoryService.gI().findItemBag(player, ID_RUA_CON);
            if (ruaCon != null && ruaCon.quantity >= 1) {
                this.createOtherMenu(player, MENU_GIAO_RUA_CON, "Cảm ơn cậu đã cứu con rùa của ta\nĐể cảm ơn ta sẽ tặng cậu món quà.", "Nhận quà", "Đóng");
            }
        }
    }

    private void handleActionMenuSelect(Player player, int select) {
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, avartar, player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
            case 1 -> handleLearnSkillStep(player);
            case 2 -> {
                if (player.clan != null) {
                    ChangeMapService.gI().changeMapNonSpaceship(player, ConstMap.LANH_DIA_BANG_HOI, Util.nextInt(100, 200), Y_LANDING_CLAN_AREA);
                }
            }
            case 3 -> {
                if (player.clan != null && player.clan.isLeader(player)) {
                    createOtherMenu(player, MENU_XAC_NHAN_GIAI_TAN, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                }
            }
            case 4 -> handleBanDoKhoBauMenu(player);
        }
    }

    private void handleLearnSkillStep(Player player) {
        if (player.LearnSkill.Time == -1) {
            ShopService.gI().opendShop(player, "QUY_LAO", false);
            return;
        }

        long time = player.LearnSkill.Time - System.currentTimeMillis();
        int ngocCost = (time / TIME_UNIT_FOR_GEM_CALC >= 2) ? (5 + (int) (time / TIME_UNIT_FOR_GEM_CALC)) : 5;
        byte levelTarget = LearnSkillService.gI().getLevelFromItem((int) player.LearnSkill.ItemTemplateSkillId);
        String skillName = SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name;

        this.createOtherMenu(player, ConstNpc.MENU_HOC_KY_NANG,
                "Con đang học kỹ năng\n" + skillName + " cấp " + levelTarget + "\nThời gian còn lại: " + TimeUtil.getTime(time),
                "Học\nCấp tốc\n" + ngocCost + " ngọc", "Huỷ", "Bỏ qua");
    }

    private void handleHocKyNangMenu(Player player, int select) {
        if (select == 0) {
            long time = player.LearnSkill.Time - System.currentTimeMillis();
            int ngocCost = (time / TIME_UNIT_FOR_GEM_CALC >= 2) ? (5 + (int) (time / TIME_UNIT_FOR_GEM_CALC)) : 5;
            if (player.inventory.gem < ngocCost) {
                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc để học cấp tốc");
                return;
            }
            player.inventory.subGem(ngocCost);
            LearnSkillService.gI().finishLearnSkill(player);
        } else if (select == 1) {
            this.createOtherMenu(player, MENU_XAC_NHAN_HUY_HOC, "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
        }
    }

    private void handleConfirmHuyHoc(Player player, int select) {
        if (select == 0) LearnSkillService.gI().cancelLearn(player);
    }

    private void handleConfirmGiaiTan(Player player, int select) {
        if (select == 0 && player.clan != null && player.clan.isLeader(player)) {
            Input.gI().createFormGiaiTanBangHoi(player);
        }
    }

    private void handleBanDoKhoBauMenu(Player player) {
        if (player.clan == null) return;
        if (player.clan.BanDoKhoBau != null) {
            this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB, "Bang hội con đang ở hang kho báu cấp " + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?", "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
        } else {
            this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB, "Đây là bản đồ kho báu hải tặc tí hon...", "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
        }
    }

    private void handleBanDoKhoBau(Player player, int select) {
        if (select != 2) return;
        if (player.clan == null) {
            Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
            return;
        }
        if (!player.isAdmin() && player.nPoint.power < TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
            this.npcChat(player, "Yêu cầu sức mạnh lớn hơn " + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
            return;
        }
        
        if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPENED_DBKB) {
            ChangeMapService.gI().goToDBKB(player);
        } else {
            Input.gI().createFormChooseLevelBDKB(player);
        }
    }

    private void handleAcceptGoToBDKB(Player player, int select) {
        if (select == 0) {
            TreasureUnderSeaService.gI().openBanDoKhoBau(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
        }
    }
}