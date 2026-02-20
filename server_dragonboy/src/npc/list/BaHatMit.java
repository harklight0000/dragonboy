package npc.list;

import combine.CheTaoCuonSachCu;
import services.CombineService;
import combine.DoiSachTuyetKy;
import combine.NangCapVatPham;
import consts.ConstDailyGift;
import consts.ConstNpc;
import consts.ConstMap;
import services.DailyGiftService;
import item.Item;
import services.map.ChangeMapService;
import matches.DeathOrAliveArena.DeathOrAliveArenaSession;
import matches.DeathOrAliveArena.DeathOrAliveArenaService;
import matches.DeathOrAliveArena.DeathOrAliveArenaManager;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import shop.ShopService;
import utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaHatMit extends Npc {

    private static final int COST_BET_VDST = 1_000_000;
    private static final int ITEM_ID_BONG_TAI_C1 = 454;
    private static final int ITEM_ID_BONG_TAI_C2 = 921;

    public BaHatMit(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.DAO_KAME -> openMenuMapDaoRua(player);
            case ConstMap.VO_DAI_HAT_MIT -> openMenuMapVoDai(player);
            default -> openMenuDefault(player);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.DAO_KAME -> confirmMenuMapDaoRua(player, select);
            case ConstMap.VO_DAI_HAT_MIT -> confirmMenuMapVoDai(player, select);
            case ConstMap.VACH_NUI_ARU_42, 
                 ConstMap.VACH_NUI_MOORI_43, 
                 ConstMap.VACH_NUI_KAKAROT, 
                 ConstMap.SIEU_THI -> confirmMenuDefault(player, select);
        }
    }

    private void openMenuMapDaoRua(Player player) {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Ngươi tìm ta có việc gì?",
                "Chức năng\npha lê",
                "Chức năng\nđệ tử",
                "Chức năng\nSét Kích Hoạt",
                "Chức năng\nItem cấp 2",
                "võ dài sinh tử"
        );
    }

    private void openMenuMapVoDai(Player player) {
        if (Util.isAfterMidnight(player.lastTimePKVoDaiSinhTu)) {
            player.haveRewardVDST = false;
            player.thoiVangVoDaiSinhTu = 0;
        }

        if (player.haveRewardVDST) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đây là phần thưởng cho con.", "1 vệ tinh\nngẫu nhiên");
            return;
        }

        DeathOrAliveArenaSession vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
        String npcSay = "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó";
        
        if (vdst != null) {
            if (vdst.getPlayer().equals(player)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Ngươi muốn hủy đăng ký thi đấu võ đài?",
                        "Top 100", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
                return;
            }
            this.createOtherMenu(player, ConstNpc.BASE_MENU, npcSay,
                    "Top 100", "Bình chọn", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
            return;
        }

        this.createOtherMenu(player, ConstNpc.BASE_MENU, npcSay,
                "Top 100", "Đồng ý\n" + player.thoiVangVoDaiSinhTu + " thỏi vàng", "Từ chối", "Về\nđảo rùa");
    }

    private void openMenuDefault(Player player) {
        List<String> menu = new ArrayList<>(Arrays.asList("Sách\nTuyệt Kỹ", "Cửa hàng\nBùa", "Nâng cấp\nVật phẩm", "Làm phép\nNhập đá", "Nhập\nNgọc Rồng"));
        
        if (InventoryService.gI().findItem(player, ITEM_ID_BONG_TAI_C1) || InventoryService.gI().findItem(player, ITEM_ID_BONG_TAI_C2)) {
            String bongTaiText = InventoryService.gI().findItemBongTaiCap2(player) ? "Mở chỉ số\nBông tai\nPorata cấp\n2" : "Nâng cấp\nBông tai\nPorata";
            menu.set(3, bongTaiText);
        }
        
        if (DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI)) {
            menu.add(0, "Thưởng\nBùa 1h\nngẫu nhiên");
        }
        
        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?", menu.toArray(new String[0]));
    }

    private void confirmMenuMapDaoRua(Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();

        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> createOtherMenu(player, 3, "Ta có thể giúp gì cho ngươi ?", "Ép sao\ntrang bị", "Pha lê\nhóa\ntrang bị", "Nâng cấp\nSao pha lê", "Đánh bóng\nSao pha lê", "Cường hóa\nlỗ sao\npha lê", "Tạo đá\nHematite", "Tạo\nDùi Đục", "Tạo\nđá mài");
                case 1 -> createOtherMenu(player, 4, "Ta có thể giúp gì cho ngươi ?", "Nâng cấp\nđệ blackgoku", "Nâng cấp\nđệ blackgoku rose");
                case 2 -> createOtherMenu(player, 5, "Ta có thể giúp gì cho ngươi ?", "Nâng cấp\nSét Kích Hoạt", "Nâng cấp\nSét Kích Hoạt Vip");
                case 3 -> createOtherMenu(player, 6, "Ta có thể giúp gì cho ngươi ?", "Nâng cấp\nItem Cấp 2");
                case 4 -> ChangeMapService.gI().changeMapNonSpaceship(player, ConstMap.VO_DAI_HAT_MIT, 200 + Util.nextInt(-100, 100), 408);
            }
            return;
        }

        switch (indexMenu) {
            case 3 -> handlePhaLeMenu(player, select);
            case 4 -> handleDeTuMenu(player, select);
            case 5 -> handleSetKichHoatMenu(player, select);
            case 6 -> { if (select == 0) CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_ITEM_CAP_2); }
            case ConstNpc.MENU_START_COMBINE -> handleStartCombineDaoRua(player, select);
        }
    }

    private void handlePhaLeMenu(Player player, int select) {
        switch (select) {
            case 0 -> CombineService.gI().openTabCombine(player, CombineService.EP_SAO_TRANG_BI);
            case 1 -> CombineService.gI().openTabCombine(player, CombineService.PHA_LE_HOA_TRANG_BI);
            case 2 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_SAO_PHA_LE);
            case 3 -> CombineService.gI().openTabCombine(player, CombineService.DANH_BONG_SAO_PHA_LE);
            case 4 -> CombineService.gI().openTabCombine(player, CombineService.CUONG_HOA_LO_SAO_PHA_LE);
            case 5 -> CombineService.gI().openTabCombine(player, CombineService.TAO_DA_HEMATITE);
            case 6 -> CombineService.gI().openTabCombine(player, CombineService.DUI_DUC);
            case 7 -> CombineService.gI().openTabCombine(player, CombineService.DA_MAI);
        }
    }

    private void handleDeTuMenu(Player player, int select) {
        switch (select) {
            case 0 -> CombineService.gI().openTabCombine(player, CombineService.NANG_DE_DE_BLACK_GOKU);
            case 1 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_DE_TU_BLACK_GOKU_ROSE);
        }
    }

    private void handleSetKichHoatMenu(Player player, int select) {
        switch (select) {
            case 0 -> CombineService.gI().openTabCombine(player, CombineService.DAP_SET_KICH_HOAT);
            case 1 -> CombineService.gI().openTabCombine(player, CombineService.DAP_SET_KICH_HOAT_VIP);
        }
    }

    private void handleStartCombineDaoRua(Player player, int select) {
        if (player.combineNew.typeCombine == CombineService.DAP_SET_KICH_HOAT_VIP) {
            switch (select) {
                case 0 -> CombineService.gI().startCombineVip(player, 10);
                case 1 -> CombineService.gI().startCombineVip(player, 100);
            }
        } else if (select == 0) {
            CombineService.gI().startCombine(player);
        }
    }

    private void confirmMenuMapVoDai(Player player, int select) {
        if (player.idMark.isBaseMenu()) {
            if (player.haveRewardVDST) {
                if (select == 0) handleRewardVDST(player);
                return;
            }

            DeathOrAliveArenaSession vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
            if (vdst != null) {
                handleVoDaiSessionLogic(player, vdst, select);
                return;
            }

            switch (select) {
                case 1 -> DeathOrAliveArenaService.gI().startChallenge(player);
                case 3 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.DAO_KAME, -1, 1156);
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.DAT_CUOC_HAT_MIT) {
            DeathOrAliveArenaSession vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
            if (vdst != null) processBet(player, vdst, select == 0);
        }
    }

    private void handleVoDaiSessionLogic(Player player, DeathOrAliveArenaSession vdst, int select) {
        if (vdst.getPlayer().equals(player)) {
            switch (select) {
                case 1 -> this.npcChat("Không thể thực hiện");
                case 3 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.DAO_KAME, -1, 1156);
            }
            return;
        }
        switch (select) {
            case 1 -> this.createOtherMenu(player, ConstNpc.DAT_CUOC_HAT_MIT,
                    "Phí bình chọn là 1 triệu vàng\nkhi trận đấu kết thúc\n90% tổng tiền bình chọn sẽ chia đều cho phe bình chọn chính xác",
                    "Bình chọn cho " + vdst.getPlayer().name + " (" + vdst.getCuocPlayer() + ")",
                    "Bình chọn cho hạt mít (" + vdst.getCuocBaHatMit() + ")");
            case 2 -> DeathOrAliveArenaService.gI().startChallenge(player);
            case 4 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.DAO_KAME, -1, 1156);
        }
    }

    private void confirmMenuDefault(Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();

        if (player.idMark.isBaseMenu()) {
            if (!DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI)) select++;
            if (!InventoryService.gI().findItem(player, ITEM_ID_BONG_TAI_C1) && !InventoryService.gI().findItem(player, ITEM_ID_BONG_TAI_C2)) {
                if (select >= 4) select++;
            }

            switch (select) {
                case 0 -> handleDailyCharm(player);
                case 1 -> createOtherMenu(player, ConstNpc.MENU_SACH_TUYET_KY, "Ta có thể giúp gì cho ngươi ?", "Đóng thành\nSách cũ", "Đổi Sách\nTuyệt kỹ", "Giám định\nSách", "Tẩy\nSách", "Nâng cấp\nSách\nTuyệt kỹ", "Hồi phục\nSách", "Phân rã\nSách");
                case 2 -> createOtherMenu(player, ConstNpc.MENU_OPTION_SHOP_BUA, "Bùa của ta rất lợi hại...", "Bùa\n1 giờ", "Bùa\n8 giờ", "Bùa\n1 tháng", "Đóng");
                case 3 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_VAT_PHAM);
                case 4 -> handleBongTaiCombine(player);
                case 5 -> CombineService.gI().openTabCombine(player, CombineService.LAM_PHEP_NHAP_DA);
                case 6 -> CombineService.gI().openTabCombine(player, CombineService.NHAP_NGOC_RONG);
            }
            return;
        }

        switch (indexMenu) {
            case ConstNpc.MENU_SACH_TUYET_KY -> handleSachTuyetKyMenu(player, select);
            case ConstNpc.DONG_THANH_SACH_CU -> CheTaoCuonSachCu.cheTaoCuonSachCu(player);
            case ConstNpc.DOI_SACH_TUYET_KY -> DoiSachTuyetKy.doiSachTuyetKy(player);
            case ConstNpc.MENU_OPTION_SHOP_BUA -> handleShopBua(player, select);
            case ConstNpc.MENU_START_COMBINE -> handleStartCombineDefault(player, select);
        }
    }

    private void handleBongTaiCombine(Player player) {
        int type = InventoryService.gI().findItemBongTaiCap2(player) ? CombineService.NANG_CHI_SO_BONG_TAI : CombineService.NANG_CAP_BONG_TAI;
        CombineService.gI().openTabCombine(player, type);
    }

    private void handleSachTuyetKyMenu(Player player, int select) {
        switch (select) {
            case 0 -> CheTaoCuonSachCu.showCombine(player);
            case 1 -> DoiSachTuyetKy.showCombine(player);
            case 2 -> CombineService.gI().openTabCombine(player, CombineService.GIAM_DINH_SACH);
            case 3 -> CombineService.gI().openTabCombine(player, CombineService.TAY_SACH);
            case 4 -> CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_SACH_TUYET_KY);
            case 5 -> CombineService.gI().openTabCombine(player, CombineService.HOI_PHUC_SACH);
            case 6 -> CombineService.gI().openTabCombine(player, CombineService.PHAN_RA_SACH);
        }
    }

    private void handleShopBua(Player player, int select) {
        switch (select) {
            case 0 -> ShopService.gI().opendShop(player, "BUA_1H", true);
            case 1 -> ShopService.gI().opendShop(player, "BUA_8H", true);
            case 2 -> ShopService.gI().opendShop(player, "BUA_1M", true);
        }
    }

    private void handleStartCombineDefault(Player player, int select) {
        if (select != 0) {
            if (player.combineNew.typeCombine == CombineService.NANG_CAP_VAT_PHAM && select == 1) {
                NangCapVatPham.nangCapVatPham(player);
            }
            return;
        }
        CombineService.gI().startCombine(player);
    }

    private void handleRewardVDST(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống, không thể nhặt thêm");
            return;
        }
        Item item = ItemService.gI().createNewItem((short) (Util.nextInt(342, 345)));
        item.itemOptions.add(new Item.ItemOption(93, 30));
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);
        player.haveRewardVDST = false;
    }

    private void processBet(Player player, DeathOrAliveArenaSession vdst, boolean isBetPlayer) {
        if (player.inventory.gold < COST_BET_VDST) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(COST_BET_VDST - player.inventory.gold) + " vàng nữa");
            return;
        }
        if (isBetPlayer) {
            vdst.setCuocPlayer(vdst.getCuocPlayer() + 1);
            player.binhChonPlayer++;
        } else {
            vdst.setCuocBaHatMit(vdst.getCuocBaHatMit() + 1);
            player.binhChonHatMit++;
        }
        vdst.addBinhChon(player);
        player.zoneBinhChon = player.zone;
        player.inventory.gold -= COST_BET_VDST;
        Service.gI().sendMoney(player);
    }

    private void handleDailyCharm(Player player) {
        if (!DailyGiftService.checkDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI)) {
            Service.gI().sendThongBao(player, "Hôm nay bạn đã nhận bùa miễn phí rồi!!!");
            return;
        }
        int idItem = Util.nextInt(213, 219);
        player.charms.addTimeCharms(idItem, 60);
        Item bua = ItemService.gI().createNewItem((short) idItem);
        Service.gI().sendThongBao(player, "Bạn vừa nhận thưởng " + bua.template.name);
        DailyGiftService.updateDailyGift(player, ConstDailyGift.NHAN_BUA_MIEN_PHI);
    }
}