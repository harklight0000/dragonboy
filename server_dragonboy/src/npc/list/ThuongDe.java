package npc.list;

import services.TrainingService;
import boss.BossID;
import consts.ConstNpc;
import consts.ConstMap;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.Service;
import services.func.LuckyRound;
import shop.ShopService;

public class ThuongDe extends Npc {

    private static final int MENU_RE_TAP_TU_DONG = 2001;
    private static final int MENU_XAC_NHAN_LUYEN_TAP = 2002;
    private static final int MENU_XAC_NHAN_THACH_DAU = 2003;
    
    private static final int X_HANH_TINH_KAIO = 354;
    private static final int X_THAN_DIEN_CDRD = 295;
    private static final int Y_THAN_DIEN_CDRD = 408;

    public ThuongDe(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.THAN_DIEN -> openMenuThanDien(player);
            case ConstMap.CON_DUONG_RAN_DOC -> openMenuConDuongRanDoc(player);
        }
    }

    private void openMenuThanDien(Player player) {
        if (checkConditionGoToKarin(player)) return;

        String tapTuDongText = player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động";

        switch (player.levelLuyenTap) {
            case 2 -> createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Pôpô là đệ tử của ta...",
                    tapTuDongText, "Tập luyện\nvới\nMr.PôPô", "Thách đấu\nMr.PôPô", "Đến\nKaio", "Quay ngọc\nMay mắn");
            case 3 -> createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Từ nay con sẽ là đệ tử của ta...",
                    tapTuDongText, "Tập luyện\nvới\nThượng Đế", "Thách đấu\nThượng Đế", "Đến\nKaio", "Quay ngọc\nMay mắn");
            default -> createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Con đã mạnh hơn ta...",
                    tapTuDongText, "Tập luyện\nvới\nMr.PôPô", "Tập luyện\nvới\nThượng Đế", "Đến\nKaio", "Quay ngọc\nMay mắn");
        }
    }

    private void openMenuConDuongRanDoc(Player player) {
        createOtherMenu(player, 0, "Hãy nắm lấy tay ta mau!", "về\nthần điện");
    }

    private boolean checkConditionGoToKarin(Player player) {
        if (player.clan != null && player.clan.ConDuongRanDoc != null && player.joinCDRD 
                && player.clan.ConDuongRanDoc.allMobsDead && !player.talkToThuongDe) {
            Service.gI().sendThongBao(player, "Hãy xuống gặp thần mèo Karin");
            createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy xuống gặp thần mèo Karin", "OK");
            return true;
        }
        return false;
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.THAN_DIEN -> handleMenuThanDien(player, select);
            case ConstMap.CON_DUONG_RAN_DOC -> handleMenuConDuongRanDoc(player, select);
        }
    }

    private void handleMenuThanDien(Player player, int select) {
        if (player.idMark.isBaseMenu()) {
            handleBaseMenuSelect(player, select);
            return;
        }

        handleSubMenuSelect(player, select);
    }

    private void handleBaseMenuSelect(Player player, int select) {
        switch (select) {
            case 0 -> handleToggleTapTuDong(player);
            case 1 -> openLuyenTapMenu(player);
            case 2 -> openThachDauMenu(player);
            case 3 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.HANH_TINH_KAIO, -1, X_HANH_TINH_KAIO);
            case 4 -> createOtherMenu(player, ConstNpc.MENU_CHOOSE_LUCKY_ROUND, "Con muốn làm gì nào?", 
                    "Quay bằng\nvàng", "Vòng quay\nđặc biệt", "Rương phụ\n(" + getBoxCrackBallCount(player) + " món)", "Xóa hết\ntrong rương", "Đóng");
        }
    }

    private void handleSubMenuSelect(Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();
        switch (indexMenu) {
            case MENU_RE_TAP_TU_DONG -> handleRegTapTuDong(player, select);
            case MENU_XAC_NHAN_LUYEN_TAP -> handleConfirmLuyenTap(player);
            case MENU_XAC_NHAN_THACH_DAU -> handleConfirmThachDau(player);
            case ConstNpc.MENU_CHOOSE_LUCKY_ROUND -> handleLuckyRoundMenu(player, select);
        }
    }

    private void handleToggleTapTuDong(Player player) {
        if (checkConditionGoToKarin(player)) {
            player.talkToThuongDe = true;
            return;
        }

        if (player.dangKyTapTuDong) {
            player.dangKyTapTuDong = false;
            NpcService.gI().createTutorial(player, tempId, avartar, "Con đã hủy thành công đăng ký tập tự động...");
            return;
        }
        createOtherMenu(player, MENU_RE_TAP_TU_DONG, "Đăng ký để mỗi khi Offline quá 30 phút...", 
                "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
    }

    private void openLuyenTapMenu(Player player) {
        String bossName = (player.levelLuyenTap == 3) ? "Thượng Đế" : "Mr.PôPô";
        int powerIncrease = (player.levelLuyenTap == 3) ? 160 : 80;
        createOtherMenu(player, MENU_XAC_NHAN_LUYEN_TAP, 
                "Con có chắc muốn tập luyện ?\nTập luyện với " + bossName + " sẽ tăng " + powerIncrease + " sức mạnh...", 
                "Đồng ý\nluyện tập", "Không\nđồng ý");
    }

    private void openThachDauMenu(Player player) {
        String msg = switch (player.levelLuyenTap) {
            case 2 -> "Nếu thắng Mr.PôPô sẽ được tập với ta, tăng 160 sức mạnh...";
            case 3 -> "Nếu thắng được ta, con sẽ được học võ với người mạnh hơn ta...";
            default -> "Tập luyện với ta sẽ tăng 160 sức mạnh...";
        };
        createOtherMenu(player, MENU_XAC_NHAN_THACH_DAU, "Con có chắc muốn thách đấu ?\n" + msg, "Đồng ý\ngiao đấu", "Không\nđồng ý");
    }

    private void handleRegTapTuDong(Player player, int select) {
        if (select == 0) NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.TAP_TU_DONG);
        if (select == 1) {
            player.mapIdDangTapTuDong = mapId;
            player.dangKyTapTuDong = true;
            NpcService.gI().createTutorial(player, tempId, avartar, "Từ giờ, quá 30 phút Offline con sẽ được tự động luyện tập");
        }
    }

    private void handleConfirmLuyenTap(Player player) {
        int bossId = (player.levelLuyenTap == 3) ? BossID.THUONG_DE : BossID.MRPOPO;
        TrainingService.gI().callBoss(player, bossId, false);
    }

    private void handleConfirmThachDau(Player player) {
        int bossId = (player.levelLuyenTap == 2) ? BossID.MRPOPO : BossID.THUONG_DE;
        boolean isThachDau = (player.levelLuyenTap == 2 || player.levelLuyenTap == 3);
        TrainingService.gI().callBoss(player, bossId, isThachDau);
    }

    private void handleLuckyRoundMenu(Player player, int select) {
        switch (select) {
            case 0 -> LuckyRound.gI().openCrackBallUI(player, LuckyRound.USING_GOLD);
            case 1 -> LuckyRound.gI().openCrackBallVipUI(player, LuckyRound.USING_GOLD);
            case 2 -> ShopService.gI().opendShop(player, "ITEMS_LUCKY_ROUND", true);
            case 3 -> NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND, this.avartar, 
                    "Con có chắc muốn xóa hết vật phẩm...?", "Đồng ý", "Hủy bỏ");
        }
    }

    private void handleMenuConDuongRanDoc(Player player, int select) {
        if (select != 0) return;
        if (player.clan == null || player.clan.ConDuongRanDoc == null || !player.clan.ConDuongRanDoc.allMobsDead) {
            Service.gI().sendThongBao(player, "Chưa hạ hết đối thủ");
            return;
        }
        ChangeMapService.gI().changeMapYardrat(player, ChangeMapService.gI().getMapCanJoin(player, ConstMap.THAN_DIEN), X_THAN_DIEN_CDRD, Y_THAN_DIEN_CDRD);
        Service.gI().sendThongBao(player, "Hãy xuống gặp thần mèo Karin");
    }

    private int getBoxCrackBallCount(Player player) {
        return player.inventory.itemsBoxCrackBall.size() - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsBoxCrackBall);
    }
}