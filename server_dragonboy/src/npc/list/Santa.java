package npc.list;

import consts.ConstNpc;
import consts.ConstMap;
import item.Item;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.func.Input;
import shop.ShopService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Santa extends Npc {

    private static final int ITEM_PHI_GIAO_DICH = 459;

    public Santa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        List<String> menu = new ArrayList<>(Arrays.asList(
                "Cửa hàng",
                "Mở rộng\nHành trang\nRương đồ",
                "Nhập mã\nquà tặng",
                "Cửa hàng\nHạn sử dụng",
                "Tiệm\nHớt tóc",
                "Danh\nhiệu",
                "Cửa hàng\nđặc biệt"
        ));

        if (hasDiscountTicket(player)) {
            menu.add(1, "Giảm giá\n80%");
        }

        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?", 
                menu.toArray(new String[0]));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || !player.idMark.isBaseMenu()) return;

        boolean isAtSantaMap = (this.mapId == ConstMap.DAO_KAME 
                             || this.mapId == ConstMap.DAO_GURU 
                             || this.mapId == ConstMap.VACH_NUI_DEN);
        
        if (!isAtSantaMap) return;

        if (hasDiscountTicket(player)) {
            handleConfirmWithDiscount(player, select);
        } else {
            handleConfirmNormal(player, select);
        }
    }

    private boolean hasDiscountTicket(Player player) {
        Item ticket = InventoryService.gI().findItem(player.inventory.itemsBag, ITEM_PHI_GIAO_DICH);
        return ticket != null && ticket.quantity >= 1;
    }

    private void handleConfirmWithDiscount(Player player, int select) {
        switch (select) {
            case 0 -> ShopService.gI().opendShop(player, "SANTA", false);
            case 1 -> ShopService.gI().opendShop(player, "SANTA_GIAM_GIA", false);
            case 2 -> ShopService.gI().opendShop(player, "SANTA_MO_RONG_HAN_TRANG", false);
            case 3 -> Input.gI().createFormGiftCode(player);
            case 4 -> ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
            case 5 -> ShopService.gI().opendShop(player, "SANTA_HEAD", false);
            case 6 -> ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
            case 7 -> ShopService.gI().opendShop(player, "SHOP_VIP", false);
        }
    }

    private void handleConfirmNormal(Player player, int select) {
        switch (select) {
            case 0 -> ShopService.gI().opendShop(player, "SANTA", false);
            case 1 -> ShopService.gI().opendShop(player, "SANTA_MO_RONG_HAN_TRANG", false);
            case 2 -> Input.gI().createFormGiftCode(player);
            case 3 -> ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
            case 4 -> ShopService.gI().opendShop(player, "SANTA_HEAD", false);
            case 5 -> ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
            case 6 -> ShopService.gI().opendShop(player, "SHOP_VIP", false);
        }
    }
}