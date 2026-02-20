package npc.list;

import services.TrainingService;
import boss.BossID;
import consts.ConstNpc;
import consts.ConstMap;
import item.Item;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import services.TaskService;
import utils.Util;

public class Karin extends Npc {

    private static final int MENU_RE_TAP_TU_DONG = 2001;
    private static final int MENU_XAC_NHAN_LUYEN_TAP = 2002;
    private static final int MENU_XAC_NHAN_THACH_DAU = 2003;
    private static final int POWER_THRESHOLD_FOR_SST = 1_000_000_000;
    private static final int ITEM_SST_SMALL = 727;
    private static final int ITEM_SST_LARGE = 728;

    public Karin(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) return;

        if (this.mapId != ConstMap.THAP_KARIN) return;

        if (player.winSTT && !Util.isAfterMidnight(player.lastTimeWinSTT)) {
            openMenuSieuThanThuy(player);
            return;
        }

        if (checkConDuongRanDocReward(player)) return;

        openMenuLuyenTap(player);
    }

    private void openMenuSieuThanThuy(Player player) {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Hãy bình tĩnh..nghe ta nói đã\nMi chưa đủ sức hạ hắn đâu!\nThôi được rồi...chờ tí\nTa sẽ cho mi uống thuốc.\nThuốc 'Tăng lực siêu thần thủy'",
                "Đồng ý");
    }

    private boolean checkConDuongRanDocReward(Player player) {
        if (player.clan == null || player.clan.ConDuongRanDoc == null || !player.joinCDRD || !player.clan.ConDuongRanDoc.allMobsDead) {
            return false;
        }

        if (player.talkToThanMeo) {
            Service.gI().sendThongBao(player, "Hãy mau bay xuống chân tháp Karin");
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy mau bay xuống chân tháp Karin", "OK");
            return true;
        }

        player.talkToThuongDe = true;
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Cầm lấy hai hạt đậu cuối cùng của ta đây\nCố giữ mình nhé " + player.name + "!",
                "Cám ơn\nsư phụ");
        return true;
    }

    private void openMenuLuyenTap(Player player) {
        String tapTuDongText = player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động";
        
        switch (player.levelLuyenTap) {
            case 0 -> this.createOtherMenu(player, ConstNpc.BASE_MENU, "Muốn chiến thắng Tàu Pảy Pảy phải đánh bại được ta đã", tapTuDongText, "Nhiệm vụ", "Tập luyện\nvới\nThần Mèo", "Thách đấu\nThần Mèo");
            case 1 -> this.createOtherMenu(player, ConstNpc.BASE_MENU, "Từ giờ Yajirô sẽ luyện tập cùng ngươi...", tapTuDongText, "Tập luyện\nvới\nYajirô", "Thách đấu\nYajirô");
            default -> this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con hãy bay theo cây Gậy Như Ý trên đỉnh tháp...", tapTuDongText, "Tập luyện\nvới\nThần Mèo", "Tập luyện\nvới\nYajirô");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || this.mapId != ConstMap.THAP_KARIN) return;

        if (player.winSTT && !Util.isAfterMidnight(player.lastTimeWinSTT)) {
            handleRewardSieuThanThuy(player);
            return;
        }

        if (handleCDRDSelect(player)) return;

        int indexMenu = player.idMark.getIndexMenu();
        if (player.idMark.isBaseMenu()) {
            handleBaseMenuSelect(player, select);
        } else {
            switch (indexMenu) {
                case MENU_RE_TAP_TU_DONG -> handleRegTapTuDong(player, select);
                case MENU_XAC_NHAN_LUYEN_TAP -> handleConfirmLuyenTap(player, select);
                case MENU_XAC_NHAN_THACH_DAU -> handleConfirmThachDau(player, select);
            }
        }
    }

    private void handleRewardSieuThanThuy(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy, cần một ô trống trong hành trang");
            return;
        }

        int itemId = player.nPoint.power < POWER_THRESHOLD_FOR_SST ? ITEM_SST_SMALL : ITEM_SST_LARGE;
        Item item = ItemService.gI().createNewItem(((short) itemId));
        item.itemOptions.add(new Item.ItemOption(30, 0));
        item.itemOptions.add(new Item.ItemOption(93, 1));

        player.callBossPocolo = false;
        player.winSTT = false;
        player.zoneSieuThanhThuy = null;
        
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);
    }

    private boolean handleCDRDSelect(Player player) {
        if (player.clan != null && player.clan.ConDuongRanDoc != null && player.joinCDRD && player.clan.ConDuongRanDoc.allMobsDead) {
            Service.gI().sendThongBao(player, "Hãy mau bay xuống chân tháp Karin");
            if (!player.talkToThanMeo) {
                player.nPoint.hp = player.nPoint.hpMax;
                player.nPoint.mp = player.nPoint.mpMax;
                Service.gI().sendInfoPlayerEatPea(player);
            }
            player.talkToThanMeo = true;
            return true;
        }
        return false;
    }

    private void handleBaseMenuSelect(Player player, int select) {
        if (select == 0) {
            handleToggleTapTuDong(player);
            return;
        }

        switch (player.levelLuyenTap) {
            case 0 -> {
                switch (select) {
                    case 2 -> createOtherMenu(player, MENU_XAC_NHAN_LUYEN_TAP, "Con có chắc muốn tập luyện ?\nTập luyện với ta sẽ tăng 20 sức mỗi phút", "Đồng ý\nluyện tập", "Không\nđồng ý");
                    case 3 -> createOtherMenu(player, MENU_XAC_NHAN_THACH_DAU, "Con có chắc muốn thách đấu ?...", "Đồng ý\ngiao đấu", "Không\nđồng ý");
                }
            }
            case 1 -> {
                switch (select) {
                    case 1 -> createOtherMenu(player, MENU_XAC_NHAN_LUYEN_TAP, "Con có chắc muốn tập luyện ?\nTập luyện với Yajirô sẽ tăng 40 sức mỗi phút", "Đồng ý\nluyện tập", "Không\nđồng ý");
                    case 2 -> createOtherMenu(player, MENU_XAC_NHAN_THACH_DAU, "Con có chắc muốn thách đấu ?...", "Đồng ý\ngiao đấu", "Không\nđồng ý");
                }
            }
            default -> {
                switch (select) {
                    case 1 -> TrainingService.gI().callBoss(player, BossID.KARIN, false);
                    case 2 -> TrainingService.gI().callBoss(player, BossID.YAJIRO, false);
                }
            }
        }
    }

    private void handleToggleTapTuDong(Player player) {
        if (player.dangKyTapTuDong) {
            player.dangKyTapTuDong = false;
            NpcService.gI().createTutorial(player, tempId, avartar, "Con đã hủy thành công đăng ký tập tự động...");
        } else {
            this.createOtherMenu(player, MENU_RE_TAP_TU_DONG, "Đăng ký để mỗi khi Offline quá 30 phút...", "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
        }
    }

    private void handleRegTapTuDong(Player player, int select) {
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.TAP_TU_DONG);
            case 1 -> {
                player.mapIdDangTapTuDong = mapId;
                player.dangKyTapTuDong = true;
                NpcService.gI().createTutorial(player, tempId, avartar, "Từ giờ, quá 30 phút Offline con sẽ được tự động luyện tập");
            }
        }
    }

    private void handleConfirmLuyenTap(Player player, int select) {
        if (select != 0) return;
        int bossId = (player.levelLuyenTap == 1) ? BossID.YAJIRO : BossID.KARIN;
        TrainingService.gI().callBoss(player, bossId, false);
    }

    private void handleConfirmThachDau(Player player, int select) {
        if (select != 0) return;
        int bossId = (player.levelLuyenTap == 1) ? BossID.YAJIRO : BossID.KARIN;
        TrainingService.gI().callBoss(player, bossId, true);
    }
}