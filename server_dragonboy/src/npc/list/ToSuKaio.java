package npc.list;

import services.TrainingService;
import boss.BossID;
import consts.ConstNpc;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import utils.Util;

public class ToSuKaio extends Npc {

    private static final int MENU_RE_TAP_TU_DONG = 2001;

    public ToSuKaio(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        String tnsmPerMinute = Util.formatNumber(TrainingService.gI().getTnsmMoiPhut(player));
        String message = String.format("Tập luyện với Tổ sư Kaio sẽ tăng %s sức mạnh mỗi phút, có thể tăng giảm tùy vào khả năng đánh quái của con", tnsmPerMinute);
        
        String autoTrainingOption = player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động";

        this.createOtherMenu(player, ConstNpc.BASE_MENU, message, autoTrainingOption, "Đồng ý\nluyện tập", "Không\nđồng ý");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        int indexMenu = player.idMark.getIndexMenu();

        if (player.idMark.isBaseMenu()) {
            handleBaseMenuSelect(player, select);
            return;
        }

        if (indexMenu == MENU_RE_TAP_TU_DONG) {
            handleAutoTrainingRegistration(player, select);
        }
    }

    private void handleBaseMenuSelect(Player player, int select) {
        switch (select) {
            case 0 -> handleAutoTrainingMenu(player);
            case 1 -> TrainingService.gI().callBoss(player, BossID.TO_SU_KAIO, false);
        }
    }

    private void handleAutoTrainingMenu(Player player) {
        if (player.dangKyTapTuDong) {
            player.dangKyTapTuDong = false;
            NpcService.gI().createTutorial(player, tempId, avartar, "Con đã hủy thành công đăng ký tập tự động\nTừ giờ con muốn tập Offline hãy tự đến đây trước");
            return;
        }
        
        showAutoTrainingRegistrationMenu(player);
    }

    private void showAutoTrainingRegistrationMenu(Player player) {
        String tnsmValue = Util.formatNumber(TrainingService.gI().getTnsmMoiPhut(player));
        String message = String.format("Đăng ký để mỗi khi Offline quá 30 phút, con sẽ được tự động luyện tập với tốc độ %s sức mạnh mỗi phút", tnsmValue);
        
        this.createOtherMenu(player, MENU_RE_TAP_TU_DONG, message, "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
    }

    private void handleAutoTrainingRegistration(Player player, int select) {
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.TAP_TU_DONG);
            case 1 -> {
                player.mapIdDangTapTuDong = mapId;
                player.dangKyTapTuDong = true;
                NpcService.gI().createTutorial(player, tempId, avartar, "Từ giờ, quá 30 phút Offline con sẽ được tự động luyện tập");
            }
        }
    }
}