package npc.list;

import consts.ConstNpc;
import npc.Npc;
import player.NPoint;
import player.Player;
import services.OpenPowerService;
import services.Service;
import utils.Util;

public class QuocVuong extends Npc {

    public QuocVuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        if (player.itemTime.isOpenPower || (player.pet != null && player.pet.itemTime.isOpenPower)) {
            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Chờ đợi là sức mạnh, đừng làm ta thất vọng", "Đóng");
            return;
        }

        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                "Bản thân", "Đệ tử", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        int indexMenu = player.idMark.getIndexMenu();
        switch (indexMenu) {
            case ConstNpc.BASE_MENU -> handleBaseMenu(player, select);
            case ConstNpc.OPEN_POWER_MYSEFT -> handleOpenPowerMyself(player, select);
            case ConstNpc.OPEN_POWER_PET -> handleOpenPowerPet(player, select);
        }
    }

    private void handleBaseMenu(Player player, int select) {
        switch (select) {
            case 0 -> {
                if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
                    String nextLimit = Util.numberToMoney(player.nPoint.getPowerNextLimit());
                    String cost = Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER);
                    this.createOtherMenu(player, ConstNpc.OPEN_POWER_MYSEFT,
                            "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của bản thân lên " + nextLimit,
                            "Nâng\ngiới hạn\nsức mạnh", "Nâng ngay\n" + cost + " vàng", "Đóng");
                } else {
                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Sức mạnh của con đã đạt tới giới hạn", "Đóng");
                }
            }
            case 1 -> {
                if (player.pet == null) {
                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Con cần có đệ tử trước", "Đóng");
                    return;
                }
                if (player.pet.nPoint.limitPower < NPoint.MAX_LIMIT) {
                    String nextLimit = Util.numberToMoney(player.pet.nPoint.getPowerNextLimit());
                    String cost = Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER);
                    this.createOtherMenu(player, ConstNpc.OPEN_POWER_PET,
                            "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của đệ tử lên " + nextLimit,
                            "Nâng ngay\n" + cost + " vàng", "Đóng");
                } else {
                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Sức mạnh của đệ con đã đạt tới giới hạn", "Đóng");
                }
            }
        }
    }

    private void handleOpenPowerMyself(Player player, int select) {
        switch (select) {
            case 0 -> OpenPowerService.gI().openPowerBasic(player);
            case 1 -> {
                if (player.inventory.gold >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                    if (OpenPowerService.gI().openPowerSpeed(player)) {
                        player.inventory.gold -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                        Service.gI().sendMoney(player);
                    }
                } else {
                    sendInsufficientGoldMess(player);
                }
            }
        }
    }

    private void handleOpenPowerPet(Player player, int select) {
        if (select != 0 || player.pet == null) return;

        if (player.inventory.gold >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
            if (OpenPowerService.gI().openPowerSpeed(player.pet)) {
                player.inventory.gold -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                Service.gI().sendMoney(player);
            }
        } else {
            sendInsufficientGoldMess(player);
        }
    }

    private void sendInsufficientGoldMess(Player player) {
        long missingGold = OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gold;
        Service.gI().sendThongBao(player, "Bạn không đủ vàng để mở, còn thiếu " + Util.numberToMoney(missingGold) + " vàng");
    }
}