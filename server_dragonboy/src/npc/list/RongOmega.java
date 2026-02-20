package npc.list;

import consts.ConstMap;
import consts.ConstNpc;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import logger.MyLogger;
import utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class RongOmega extends Npc {

    public RongOmega(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;

        boolean isAtSpaceShipStation = (this.mapId == ConstMap.TRAM_TAU_VU_TRU 
                                     || this.mapId == ConstMap.TRAM_TAU_VU_TRU_25 
                                     || this.mapId == ConstMap.TRAM_TAU_VU_TRU_26);
        
        if (!isAtSpaceShipStation) return;

        try {
            if (TimeUtil.isBlackBallWarOpen()) {
                handleOpenMenuWarOpen(player);
            } else {
                handleOpenMenuWarClosed(player);
            }
        } catch (Exception ex) {
            MyLogger.logWarning("Lỗi mở menu rồng Omega");
        }
    }

    private void handleOpenMenuWarOpen(Player player) {
        if (TimeUtil.isBlackBallWarCanPick() && hasBlackBallReward(player)) {
            this.createOtherMenu(player, ConstNpc.MENU_OPEN_BDW, 
                    "Ngươi đang có phần thưởng ngọc sao đen, có muốn nhận không?",
                    "Hướng\ndẫn\nthêm", "Tham gia", "Nhận\nthưởng", "Từ chối");
            return;
        }
        
        this.createOtherMenu(player, ConstNpc.MENU_OPEN_BDW, 
                "Đường đến với ngọc rồng sao đen đã mở, ngươi có muốn tham gia không?",
                "Hướng\ndẫn\nthêm", "Tham gia", "Từ chối");
    }

    private void handleOpenMenuWarClosed(Player player) {
        if (hasBlackBallReward(player)) {
            this.createOtherMenu(player, ConstNpc.MENU_NOT_OPEN_BDW, 
                    "Ngươi đang có phần thưởng ngọc sao đen, có muốn nhận không?",
                    "Hướng\ndẫn\nthêm", "Nhận\nthưởng", "Từ chối");
            return;
        }
        
        this.createOtherMenu(player, ConstNpc.MENU_NOT_OPEN_BDW,
                "Ta có thể giúp gì cho ngươi?", "Hướng\ndẫn\nthêm", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        int indexMenu = player.idMark.getIndexMenu();
        switch (indexMenu) {
            case ConstNpc.MENU_REWARD_BDW -> player.rewardBlackBall.getRewardSelect((byte) select);
            case ConstNpc.MENU_OPEN_BDW -> handleConfirmWarOpen(player, select);
            case ConstNpc.MENU_NOT_OPEN_BDW -> handleConfirmWarClosed(player, select);
        }
    }

    private void handleConfirmWarOpen(Player player, int select) {
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BLACK_BALL_WAR);
            case 1 -> {
                player.idMark.setTypeChangeMap(ConstMap.CHANGE_BLACK_BALL);
                ChangeMapService.gI().openChangeMapTab(player);
            }
            case 2 -> openRewardSelectionMenu(player);
        }
    }

    private void handleConfirmWarClosed(Player player, int select) {
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BLACK_BALL_WAR);
            case 1 -> openRewardSelectionMenu(player);
        }
    }

    private void openRewardSelectionMenu(Player player) {
        List<String> options = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (player.rewardBlackBall.timeOutOfDateReward[i] > System.currentTimeMillis()) {
                options.add("Nhận\nthưởng\n" + (i + 1) + " sao");
            }
        }

        if (!options.isEmpty()) {
            this.createOtherMenu(player, ConstNpc.MENU_REWARD_BDW, 
                    "Ngươi đang có phần thưởng ngọc sao đen, có muốn nhận không?",
                    options.toArray(new String[0]));
        }
    }

    private boolean hasBlackBallReward(Player player) {
        for (int i = 0; i < 7; i++) {
            if (player.rewardBlackBall.timeOutOfDateReward[i] > System.currentTimeMillis()) {
                return true;
            }
        }
        return false;
    }
}