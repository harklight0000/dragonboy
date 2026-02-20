package npc.list;

import consts.ConstNpc;
import consts.ConstMap;
import matches.SuperRank.SuperRankManager;
import services.map.ChangeMapService;
import services.map.NpcService;
import matches.SuperRank.SuperRankService;
import npc.Npc;
import player.Player;

public class TrongTai extends Npc {

    private static final int MAP_DHVT_CENTER = 52;
    private static final int Y_LANDING_DHVT = 336;

    public TrongTai(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        if (this.mapId != ConstMap.DAI_HOI_VO_THUAT_113) {
            super.openBaseMenu(player);
            return;
        }

        if (SuperRankManager.gI().awaiting(player)) {
            openAwaitingMenu(player);
        } else {
            openMainSuperRankMenu(player);
        }
    }

    private void openAwaitingMenu(Player player) {
        int ordinal = SuperRankManager.gI().ordinal(player.id);
        createOtherMenu(player, ConstNpc.BASE_MENU, 
                "Vui lòng chờ, số thứ tự của bạn là " + ordinal, 
                "OK", "Về\nĐại Hội\nVõ Thuật");
    }

    private void openMainSuperRankMenu(Player player) {
        String ticketMsg = player.superRank.ticket > 0 
                ? "Miễn phí\nCòn " + player.superRank.ticket + " vé" 
                : "Thi đấu";
        
        String npcSay = "Đại hội võ thuật Siêu Hạng\ndiễn ra 24/7 kể cả ngày lễ và chủ nhật\nHãy thi đấu ngay để khẳng định đẳng cấp của mình nhé";
        
        createOtherMenu(player, ConstNpc.BASE_MENU, npcSay,
                "Top 100\nCao Thủ", 
                "Hướng\ndẫn\nthêm", 
                ticketMsg, 
                "Ưu tiên\nđấu ngay", 
                "Về\nĐại Hội\nVõ Thuật");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || !player.idMark.isBaseMenu()) return;
        
        if (this.mapId != ConstMap.DAI_HOI_VO_THUAT_113) return;

        if (SuperRankManager.gI().awaiting(player)) {
            handleAwaitingSelect(player, select);
        } else {
            handleMainSelect(player, select);
        }
    }

    private void handleAwaitingSelect(Player player, int select) {
        if (select == 1) {
            goBackToDHVT(player);
        }
    }

    private void handleMainSelect(Player player, int select) {
        switch (select) {
            case 0 -> SuperRankService.gI().topList(player, 0);
            case 1 -> NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.THONG_TIN_SIEU_HANG);
            case 2 -> SuperRankService.gI().topList(player, 1);
            case 3 -> SuperRankService.gI().topList(player, 2);
            case 4 -> goBackToDHVT(player);
        }
    }

    private void goBackToDHVT(Player player) {
        ChangeMapService.gI().changeMapNonSpaceship(player, MAP_DHVT_CENTER, player.location.x, Y_LANDING_DHVT);
    }
}