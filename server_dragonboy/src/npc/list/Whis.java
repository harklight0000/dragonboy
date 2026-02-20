package npc.list;

import boss.BossID;
import consts.ConstNpc;
import consts.ConstMap;
import item.Item;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.CombineService;
import services.TrainingService;
import player.skill.LearnSkillService;
import shop.ShopService;

public class Whis extends Npc {

    private static final int ID_BI_KIP_TUYET_KY = 1229;
    private static final int MENU_CHE_TAO_THIEN_SU = 5;
    private static final int MENU_HOC_NHAN_SKILL = 12;
    private static final int MENU_HUY_HOC_SKILL = 13;
    private static final int MENU_XAC_NHAN_TUYET_KY = 6;

    public Whis(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private Item hasBiKipTuyetKy(Player pl) {
        return InventoryService.gI().findItemBag(pl, ID_BI_KIP_TUYET_KY);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        if (this.mapId != ConstMap.HANH_TINH_BILL) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đi chỗ khác chơi?", "Dạ");
            return;
        }

        Item bikip = hasBiKipTuyetKy(player);
        String levelTop = "[LV:" + (player.traning.getTop() + 1) + "]";
        String npcSay = "Thử đánh với ta xem nào.\nNgươi còn 1 lượt nữa cơ mà.";

        if (bikip != null) {
            createOtherMenu(player, ConstNpc.BASE_MENU, npcSay, "Nói chuyện", "Học\nkỹ năng", "Học\ntuyệt kỹ", levelTop);
        } else {
            createOtherMenu(player, ConstNpc.BASE_MENU, npcSay, "Nói chuyện", "Học\nkỹ năng", levelTop);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || this.mapId != ConstMap.HANH_TINH_BILL) return;

        if (player.idMark.isBaseMenu()) {
            handleBaseMenu(player, select);
        } else {
            handleSubMenu(player, select);
        }
    }

    private void handleBaseMenu(Player player, int select) {
        Item bikip = hasBiKipTuyetKy(player);

        switch (select) {
            case 0 -> createOtherMenu(player, MENU_CHE_TAO_THIEN_SU, 
                    "Ta sẽ giúp ngươi chế tạo trang bị thiên sứ", "Shop thiên sứ", "Chế tạo", "Từ chối");
            case 1 -> handleLearnSkillLogic(player);
            case 2 -> {
                if (bikip != null) LearnSkillService.gI().openHocTuyetKyMenu(this, player, bikip);
                else TrainingService.gI().callBoss(player, BossID.WHIS, false);
            }
            case 3 -> {
                if (bikip != null) TrainingService.gI().callBoss(player, BossID.WHIS, false);
            }
        }
    }

    private void handleLearnSkillLogic(Player player) {
        if (player.LearnSkill.Time != -1) {
            if (player.LearnSkill.Time <= System.currentTimeMillis()) {
                LearnSkillService.gI().finishLearnSkill(player);
            } else {
                LearnSkillService.gI().openHocNhanhMenu(this, player);
            }
        } else {
            ShopService.gI().opendShop(player, "QUY_LAO", false);
        }
    }

    private void handleSubMenu(Player player, int select) {
        int index = player.idMark.getIndexMenu();
        switch (index) {
            case MENU_CHE_TAO_THIEN_SU -> {
                if (select == 0) ShopService.gI().opendShop(player, "THIEN_SU", false);
                else if (select == 1) handleCombineThienSu(player);
            }
            case MENU_XAC_NHAN_TUYET_KY -> {
                if (select == 0) LearnSkillService.gI().confirmHocTuyetKy(this, player);
            }
            case MENU_HOC_NHAN_SKILL -> {
                if (select == 0) LearnSkillService.gI().finishLearnSkillFast(player);
                else if (select == 1) {
                    createOtherMenu(player, MENU_HUY_HOC_SKILL, 
                            "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
                }
            }
            case MENU_HUY_HOC_SKILL -> {
                if (select == 0) LearnSkillService.gI().cancelLearn(player);
            }
        }
    }

    private void handleCombineThienSu(Player player) {
        if (!player.setClothes.checkSetDes()) {
            this.createOtherMenu(player, ConstNpc.IGNORE_MENU, 
                    "Ngươi hãy trang bị đủ 5 món trang bị Hủy Diệt rồi ta nói chuyện tiếp.", "OK");
        } else {
            CombineService.gI().openTabCombine(player, CombineService.CHE_TAO_TRANG_BI_THIEN_SU);
        }
    }
}