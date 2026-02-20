package npc.list;

import services.NgocRongNamecService;
import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstMap;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.TaskService;
import services.shenron.SummonDragonNamek;
import shop.ShopService;
import utils.TimeUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Dende extends Npc {

    private static final int MENU_CALL_DRAGON = 1;
    private static final int ID_NR_NAMEC_1_SAO = 353;
    private static final int CLEAN_GEM_TIME_MS = 600_000;
    private static final int HOUR_OPEN_SUMMON = 8;
    private static final int HOUR_CLOSE_SUMMON = 22;

    public Dende(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player) || TaskService.gI().checkDoneTaskTalkNpc(player, this)) return;

        if (player.idNRNM != -1) {
            if (this.mapId == ConstMap.LANG_MORI) {
                createOtherMenu(player, MENU_CALL_DRAGON, 
                        "Ồ, ngọc rồng namếc, bạn thật là may mắn\nnếu tìm đủ 7 viên sẽ được Rồng Thiêng Namếc ban cho điều ước", 
                        "Hướng\ndẫn\nGọi Rồng", "Gọi rồng", "Từ chối");
            }
            return;
        }

        if (player.gender != ConstPlayer.NAMEC) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Xin lỗi anh, em chỉ bán đồ cho dân tộc Namếc");
            return;
        }

        openShopMenu(player);
    }

    private void openShopMenu(Player player) {
        List<String> menu = new ArrayList<>();
        menu.add("Cửa\nhàng");
        if (!player.inventory.itemsDaBan.isEmpty()) {
            menu.add("Mua lại vật phẩm đã bán");
        }
        createOtherMenu(player, ConstNpc.BASE_MENU, "Anh cần trang bị gì cứ đến chỗ em nhé", menu.toArray(String[]::new));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        int indexMenu = player.idMark.getIndexMenu();
        if (player.idMark.isBaseMenu()) {
            handleBaseMenuSelect(player, select);
            return;
        }

        if (indexMenu == MENU_CALL_DRAGON) {
            handleCallDragonMenu(player, select);
        }
    }

    private void handleBaseMenuSelect(Player player, int select) {
        switch (select) {
            case 0 -> {
                if (player.gender == ConstPlayer.NAMEC) {
                    ShopService.gI().opendShop(player, "DENDE", true);
                } else {
                    createOtherMenu(player, ConstNpc.IGNORE_MENU, "Xin lỗi anh, em chỉ bán đồ cho dân tộc Namếc", "Đóng");
                }
            }
            case 1 -> {
                if (!player.inventory.itemsDaBan.isEmpty()) {
                    ShopService.gI().opendShop(player, "ITEMS_DABAN", true);
                }
            }
        }
    }

    private void handleCallDragonMenu(Player player, int select) {
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_NRNM);
            case 1 -> processSummonDragon(player);
        }
    }

    private void processSummonDragon(Player player) {
        if (this.mapId != ConstMap.LANG_MORI || player.idNRNM == -1) return;

        if (player.idNRNM != ID_NR_NAMEC_1_SAO) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Anh phải có viên Ngọc Rồng Namek 1 sao");
            return;
        }

        int currHour = TimeUtil.getCurrHour();
        if (currHour < HOUR_OPEN_SUMMON || currHour > HOUR_CLOSE_SUMMON) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, 
                    "Xin lỗi mấy anh, em đang bận buôn bán nên chỉ rảnh gọi Rồng vào khoảng 8h đến 22h");
            return;
        }

        if (!Util.canDoWithTime(player.lastTimePickNRNM, CLEAN_GEM_TIME_MS)) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, 
                    "Ngọc bẩn quá, xin chờ em " + TimeUtil.getTimeLeft(player.lastTimePickNRNM, CLEAN_GEM_TIME_MS / 1000) + " nữa để lau bóng ngọc");
            return;
        }

        if (!NgocRongNamecService.gI().canCallDragonNamec(player)) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Hãy gom đủ 7 viên Ngọc Rồng tại đây");
            return;
        }

        executeSummon(player);
    }

    private void executeSummon(Player player) {
        NgocRongNamecService.gI().tOpenNrNamec = System.currentTimeMillis() + 86400000;
        NgocRongNamecService.gI().firstNrNamec = true;
        NgocRongNamecService.gI().timeNrNamec = 0;
        NgocRongNamecService.gI().doneDragonNamec();
        NgocRongNamecService.gI().initNgocRongNamec((byte) 1);
        NgocRongNamecService.gI().reInitNrNamec(86399000L);
        SummonDragonNamek.gI().summonNamec(player);
    }
}