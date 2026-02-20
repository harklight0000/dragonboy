package npc.list;

import consts.ConstNpc;
import consts.ConstMap;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.TaskService;
import shop.ShopService;

public class Bill extends Npc {

    private static final int MENU_OPEN_SHOP_BILL = 2;
    private static final int X_THANH_DIA_KAIO = 318;
    private static final int Y_THANH_DIA_KAIO = 336;

    public Bill(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        TaskService.gI().checkDoneTaskTalkNpc(player, this);

        if (this.mapId == ConstMap.HANH_TINH_BILL) {
            createOtherMenu(player, ConstNpc.BASE_MENU, "...", "Về\nthánh địa\nKaio", "Từ chối");
            return;
        }

        createOtherMenu(player, ConstNpc.BASE_MENU,
                "Chưa tới giờ thi đấu, xem hướng dẫn để biết thêm chi tiết",
                "Nói\nchuyện", "Hướng\ndẫn\nthêm", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.HANH_TINH_KAIO -> handleMenuKaio(player, select);
            case ConstMap.HANH_TINH_BILL -> handleMenuBillMap(player, select);
        }
    }

    private void handleMenuKaio(Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();

        switch (indexMenu) {
            case ConstNpc.BASE_MENU -> {
                switch (select) {
                    case 0 -> handleTalkWithBill(player);
                    case 1 -> NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BILL);
                }
            }
            case MENU_OPEN_SHOP_BILL -> {
                if (select == 0 && InventoryService.gI().canOpenBillShop(player)) {
                    ShopService.gI().opendShop(player, "BILL", true);
                }
            }
        }
    }

    private void handleTalkWithBill(Player player) {
        if (InventoryService.gI().canOpenBillShop(player) || player.isAdmin()) {
            createOtherMenu(player, MENU_OPEN_SHOP_BILL,
                    "Đói bụng quá...ngươi mang cho ta 99 phần đồ ăn\nta sẽ cho một món đồ Hủy Diệt.\nNếu tâm trạng ta vui ngươi có thể nhận trang bị tăng đến 15%",
                    "OK", "Từ chối");
            return;
        }

        createOtherMenu(player, MENU_OPEN_SHOP_BILL,
                "Ngươi trang bị đủ bộ 5 món trang bị Thần\nvà mang 99 phần đồ ăn tới đây...\nrồi ta nói chuyện tiếp.", "OK");
    }

    private void handleMenuBillMap(Player player, int select) {
        if (select == 0) {
            ChangeMapService.gI().changeMap(player, ConstMap.THANH_DIA_KAIO, -1, X_THANH_DIA_KAIO, Y_THANH_DIA_KAIO);
        }
    }
}