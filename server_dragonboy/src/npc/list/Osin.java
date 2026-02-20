package npc.list;

import services.dungeon.MajinBuu14HService;
import services.dungeon.MajinBuuService;
import consts.ConstNpc;
import consts.ConstMap;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.ItemTimeService;
import services.Service;
import services.TaskService;
import utils.TimeUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Osin extends Npc {

    private static final int COST_PHU_HO = 10;
    private static final int COST_GIAI_TRU_PHEP = 1;
    private static final int FLAG_MABU_SIDE = 9;
    private static final int AVATAR_BABIDI = 4388;

    public Osin(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        TaskService.gI().checkDoneTaskTalkNpc(player, this);

        switch (this.mapId) {
            case ConstMap.THANH_DIA_KAIO -> createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?", "Đến\nKaio", "Đến\nhành tinh\nBill", "Từ chối");
            case ConstMap.HANH_TINH_BILL -> createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?", "Đến\nhành tinh\nngục tù", "Từ chối");
            case ConstMap.HANH_TINH_NGUC_TU -> createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?", "Quay về", "Từ chối");
            case ConstMap.DAI_HOI_VO_THUAT -> openMenuMabuAtDHVT(player);
            case ConstMap.CONG_PHI_THUYEN, ConstMap.PHONG_CHO, ConstMap.CUA_AI_1, ConstMap.CUA_AI_2, ConstMap.CUA_AI_3, ConstMap.PHONG_CHI_HUY -> openMenuInsideMabuDungeon(player);
            case ConstMap.CONG_PHI_THUYEN_127 -> openMenuPhuHoMabu(player);
            default -> super.openBaseMenu(player);
        }
    }

    private void openMenuMabuAtDHVT(Player player) {
        player.fightMabu.clear();
        if (TimeUtil.isMabu14HOpen()) {
            createOtherMenu(player, ConstNpc.MENU_OPEN_MMB, "Mabư đã thoát khỏi vỏ bọc\nmau đi cùng ta ngăn chặn hắn lại...", "OK", "Từ chối");
        } else if (TimeUtil.isMabuOpen()) {
            createOtherMenu(player, ConstNpc.MENU_OPEN_MMB, "Bây giờ tôi sẽ bí mật...\nđuổi theo 2 tên đồ tể...", "OK", "Từ chối");
        } else {
            createOtherMenu(player, ConstNpc.MENU_NOT_OPEN_MMB, "Vào lúc " + MajinBuuService.HOUR_OPEN_MAP_MABU + "h tôi sẽ bí mật...", "OK", "Từ chối");
        }
    }

    private void openMenuInsideMabuDungeon(Player player) {
        if (player.cFlag != FLAG_MABU_SIDE) {
            NpcService.gI().createTutorial(player, tempId, avartar, "Ngươi hãy về phe của mình mà thể hiện");
            return;
        }
        List<String> menu = new ArrayList<>();
        menu.add("Hướng\ndẫn\nthêm");
        if (!player.itemTime.isUseGTPT) menu.add("Giải trừ\nphép thuật\n" + COST_GIAI_TRU_PHEP + " ngọc");
        if (player.fightMabu.pointMabu >= player.fightMabu.POINT_MAX && this.mapId != ConstMap.PHONG_CHI_HUY) menu.add("Xuống\nTầng dưới");
        
        createOtherMenu(player, ConstNpc.GO_UPSTAIRS_MENU, "Đừng vội xem thường Babiđây...", menu.toArray(String[]::new));
    }

    private void openMenuPhuHoMabu(Player player) {
        String npcSay = player.isPhuHoMapMabu ? "Ta có thể giúp gì cho ngươi ?" : "Ta sẽ phù hộ ngươi bằng\nnguồn sức mạnh của Thần Kaiô...";
        List<String> menu = new ArrayList<>();
        if (!player.isPhuHoMapMabu) menu.add("Phù hộ\n" + COST_PHU_HO + " ngọc");
        menu.add("Từ chối");
        menu.add("Về\nĐại Hội\nVõ Thuật");
        createOtherMenu(player, ConstNpc.BASE_MENU, npcSay, menu.toArray(String[]::new));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.THANH_DIA_KAIO -> handleMenuThanhDia(player, select);
            case ConstMap.HANH_TINH_BILL -> handleMenuHanhTinhBill(player, select);
            case ConstMap.HANH_TINH_NGUC_TU -> handleMenuNgucTu(player, select);
            case ConstMap.DAI_HOI_VO_THUAT -> handleMenuMabuDHVT(player, select);
            case ConstMap.CONG_PHI_THUYEN, ConstMap.PHONG_CHO, ConstMap.CUA_AI_1, ConstMap.CUA_AI_2, ConstMap.CUA_AI_3, ConstMap.PHONG_CHI_HUY -> handleMenuInsideMabu(player, select);
            case ConstMap.CONG_PHI_THUYEN_127 -> handleMenuPhuHo(player, select);
        }
    }

    private void handleMenuThanhDia(Player player, int select) {
        if (!player.idMark.isBaseMenu()) return;
        switch (select) {
            case 0 -> ChangeMapService.gI().changeMap(player, ConstMap.HANH_TINH_KAIO, -1, 354, 240);
            case 1 -> ChangeMapService.gI().changeMap(player, ConstMap.HANH_TINH_BILL, -1, 200, 312);
        }
    }

    private void handleMenuHanhTinhBill(Player player, int select) {
        if (player.idMark.isBaseMenu() && select == 0) {
            ChangeMapService.gI().changeMap(player, ConstMap.HANH_TINH_NGUC_TU, -1, 111, 792);
        }
    }

    private void handleMenuNgucTu(Player player, int select) {
        if (player.idMark.isBaseMenu() && select == 0) {
            ChangeMapService.gI().changeMap(player, ConstMap.HANH_TINH_BILL, -1, 200, 312);
        }
    }

    private void handleMenuMabuDHVT(Player player, int select) {
        if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPEN_MMB && select == 0) {
            if (TimeUtil.isMabu14HOpen()) MajinBuu14HService.gI().joinMaBu2H(player);
            else if (TimeUtil.isMabuOpen()) ChangeMapService.gI().changeMap(player, ConstMap.CONG_PHI_THUYEN, -1, Util.nextInt(100, 500), 336);
        }
    }

    private void handleMenuInsideMabu(Player player, int select) {
        if (player.cFlag != FLAG_MABU_SIDE) return;
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, AVATAR_BABIDI, ConstNpc.HUONG_DAN_MAP_MA_BU);
            case 1 -> {
                if (!player.itemTime.isUseGTPT) executeGiaiTruPhep(player);
                else tryGoDownstairs(player);
            }
            case 2 -> tryGoDownstairs(player);
        }
    }

    private void executeGiaiTruPhep(Player player) {
        player.itemTime.lastTimeUseGTPT = System.currentTimeMillis();
        player.itemTime.isUseGTPT = true;
        ItemTimeService.gI().sendAllItemTime(player);
        Service.gI().sendThongBao(player, "Phép thuật đã được giải trừ...");
    }

    private void tryGoDownstairs(Player player) {
        if (player.fightMabu.pointMabu >= player.fightMabu.POINT_MAX && this.mapId != ConstMap.PHONG_CHI_HUY) {
            ChangeMapService.gI().changeMap(player, this.map.mapIdNextMabu((short) this.mapId), -1, this.cx, this.cy);
        }
    }

    private void handleMenuPhuHo(Player player, int select) {
        int actualSelect = player.isPhuHoMapMabu ? select + 1 : select;
        switch (actualSelect) {
            case 0 -> executePhuHo(player);
            case 2 -> ChangeMapService.gI().changeMap(player, ConstMap.DAI_HOI_VO_THUAT, -1, Util.nextInt(100, 300), 336);
        }
    }

    private void executePhuHo(Player player) {
        if (player.inventory.getGem() < COST_PHU_HO) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
            return;
        }
        player.inventory.subGem(COST_PHU_HO);
        player.isPhuHoMapMabu = true;
        player.nPoint.calPoint();
        player.nPoint.setHp(player.nPoint.hpMax);
        player.nPoint.setMp(player.nPoint.mpMax);
        Service.gI().point(player);
        Service.gI().Send_Info_NV(player);
        Service.gI().Send_Caitrang(player);
    }
}