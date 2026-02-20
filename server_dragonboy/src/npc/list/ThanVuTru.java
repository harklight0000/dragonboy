package npc.list;

import services.dungeon.SnakeWayService;
import services.TrainingService;
import boss.BossID;
import consts.ConstNpc;
import consts.ConstMap;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.func.Input;
import utils.TimeUtil;

import static npc.NpcFactory.PLAYERID_OBJECT;

public class ThanVuTru extends Npc {

    private static final int MENU_RE_TAP_TU_DONG = 2001;
    private static final int MENU_XAC_NHAN_LUYEN_TAP = 2002;
    private static final int MENU_XAC_NHAN_THACH_DAU = 2003;
    private static final int MENU_CON_DUONG_RAN_DOC = 2;
    private static final int MENU_VAO_CDRD = 3;
    
    private static final int X_THAN_DIEN = 354;
    private static final int X_THANH_DIA_KAIO = 318;
    private static final int Y_THANH_DIA_KAIO = 336;

    public ThanVuTru(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        if (this.mapId != ConstMap.HANH_TINH_KAIO) return;

        String tapTuDongText = player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động";

        switch (player.levelLuyenTap) {
            case 4 -> createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Thượng đế đưa ngươi đến đây, chắc muốn ta dạy võ chứ gì\nBắt được con khỉ Bubbles rồi hãy tính",
                    tapTuDongText, "Tập luyện\nvới\nBubbles", "Thách đấu\nBubbles", "Di chuyển");
            case 5 -> createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Ta là Thần Vũ Trụ Phương Bắc cai quản khu vực bắc vũ trụ...",
                    tapTuDongText, "Tập luyện\nvới\nThần Vũ Trụ", "Thách đấu\nThần Vũ Trụ", "Di chuyển");
            default -> createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Con mạnh nhất phía bắc vũ trụ này rồi đấy...",
                    tapTuDongText, "Tập luyện\nvới\nBubbles", "Tập luyện\nvới\nThần Vũ Trụ", "Di chuyển");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || this.mapId != ConstMap.HANH_TINH_KAIO) return;

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
            case 3 -> createOtherMenu(player, ConstNpc.MENU_DI_CHUYEN, "Ta sẽ đưa con đi", 
                    "Về\nthần điện", "Thánh địa\nKaio", "Con\nđường\nrắn độc", "Từ chối");
        }
    }

    private void handleSubMenuSelect(Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();
        switch (indexMenu) {
            case MENU_RE_TAP_TU_DONG -> handleRegTapTuDong(player, select);
            case MENU_XAC_NHAN_LUYEN_TAP -> handleConfirmLuyenTap(player);
            case MENU_XAC_NHAN_THACH_DAU -> handleConfirmThachDau(player);
            case ConstNpc.MENU_DI_CHUYEN -> handleMoveMenu(player, select);
            case MENU_CON_DUONG_RAN_DOC -> handleCDRDSelect(player, select);
            case MENU_VAO_CDRD -> handleJoinCDRD(player, select);
        }
    }

    private void handleToggleTapTuDong(Player player) {
        if (player.dangKyTapTuDong) {
            player.dangKyTapTuDong = false;
            NpcService.gI().createTutorial(player, tempId, avartar, "Con đã hủy thành công đăng ký tập tự động...");
            return;
        }
        createOtherMenu(player, MENU_RE_TAP_TU_DONG, "Đăng ký để mỗi khi Offline quá 30 phút...", 
                "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
    }

    private void openLuyenTapMenu(Player player) {
        String msg = (player.levelLuyenTap == 5) ? "Tập luyện với ta sẽ tăng 640 sức mạnh..." : "Tập luyện với Khỉ Bubbles sẽ tăng 320 sức mạnh...";
        createOtherMenu(player, MENU_XAC_NHAN_LUYEN_TAP, "Con có chắc muốn tập luyện ?\n" + msg, "Đồng ý\nluyện tập", "Không\nđồng ý");
    }

    private void openThachDauMenu(Player player) {
        String msg = switch (player.levelLuyenTap) {
            case 4 -> "Nếu thắng Khỉ Bubbles sẽ được tập với ta, tăng 640 sức mạnh...";
            case 5 -> "Nếu thắng được ta, con sẽ được học võ với người mạnh hơn...";
            default -> "Tập luyện với ta sẽ tăng 640 sức mạnh...";
        };
        createOtherMenu(player, MENU_XAC_NHAN_THACH_DAU, "Con có chắc muốn thách đấu ?\n" + msg, "Đồng ý\ngiao đấu", "Không\nđồng ý");
    }

    private void handleMoveMenu(Player player, int select) {
        switch (select) {
            case 0 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.THAN_DIEN, -1, X_THAN_DIEN);
            case 1 -> ChangeMapService.gI().changeMap(player, ConstMap.THANH_DIA_KAIO, -1, X_THANH_DIA_KAIO, Y_THANH_DIA_KAIO);
            case 2 -> handleCDRDNavigation(player);
        }
    }

    private void handleCDRDNavigation(Player player) {
        if (player.clan == null) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Hãy vào bang hội trước");
            return;
        }
        
        if (player.clan.ConDuongRanDoc != null) {
            String msg = "Bang hội con đang ở con đường rắn độc cấp độ " + player.clan.ConDuongRanDoc.level 
                    + "\ncon có muốn đi cùng họ không? (" + TimeUtil.convertTimeNow(player.clan.ConDuongRanDoc.getLastTimeOpen()) + " trước)";
            createOtherMenu(player, MENU_CON_DUONG_RAN_DOC, msg, "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
            return;
        }
        
        createOtherMenu(player, MENU_CON_DUONG_RAN_DOC, "Hãy mau trở về bằng con đường rắn độc\nbọn Xayda đã đến Trái Đất", 
                "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
    }

    private void handleCDRDSelect(Player player, int select) {
        if (select != 2 || player.clan == null) return;

        if (player.clanMember.getNumDateFromJoinTimeToToday() < 1) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Gia nhập bang hội trên 1 ngày mới được tham gia");
            return;
        }

        if (player.clan.ConDuongRanDoc == null) {
            Input.gI().createFormChooseLevelCDRD(player);
        } else {
            SnakeWayService.gI().openConDuongRanDoc(player, (byte) 0);
        }
    }

    private void handleJoinCDRD(Player player, int select) {
        if (select != 0) return;
        byte level = (player.clan.ConDuongRanDoc != null) ? 0 : Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id)));
        SnakeWayService.gI().openConDuongRanDoc(player, level);
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
        int bossId = (player.levelLuyenTap == 5) ? BossID.THAN_VU_TRU : BossID.KHI_BUBBLES;
        TrainingService.gI().callBoss(player, bossId, false);
    }

    private void handleConfirmThachDau(Player player) {
        int bossId = (player.levelLuyenTap == 4) ? BossID.KHI_BUBBLES : BossID.THAN_VU_TRU;
        boolean isThachDau = (player.levelLuyenTap == 4 || player.levelLuyenTap == 5);
        TrainingService.gI().callBoss(player, bossId, isThachDau);
    }
}