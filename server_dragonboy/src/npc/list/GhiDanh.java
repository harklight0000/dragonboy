package npc.list;

import consts.ConstNpc;
import consts.ConstMap;
import item.Item;
import services.map.ChangeMapService;
import services.map.NpcService;
import matches.The23rdMartialArtCongress.The23rdMartialArtCongressService;
import matches.WorldMartialArtsTournamen.WorldMartialArtsTournamentService;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.PlayerService;
import services.ItemService;
import services.Service;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class GhiDanh extends Npc {

    private static final int ID_RUONG_GO = 570;
    private static final int MAX_LEVEL_CHEST = 12;
    private static final int MENU_REWARD_CHEST = 1;
    private static final int Y_LANDING_DHVT = 336;

    public GhiDanh(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (!canOpenNpc(pl)) return;

        switch (this.mapId) {
            case ConstMap.DAI_HOI_VO_THUAT -> WorldMartialArtsTournamentService.menu(this, pl);
            case ConstMap.DAI_HOI_VO_THUAT_129 -> openMenuDHVT23(pl);
            default -> super.openBaseMenu(pl);
        }
    }

    private void openMenuDHVT23(Player pl) {
        if (Util.isAfterMidnight(pl.lastTimePKDHVT23)) {
            pl.goldChallenge = 50_000;
            pl.rubyChallenge = 2;
            pl.levelWoodChest = 0;
        }

        List<String> menu = new ArrayList<>();
        menu.add("Hướng\ndẫn\nthêm");
        menu.add("Thi đấu\n" + Util.numberToMoney(pl.rubyChallenge) + " ngọc");
        menu.add("Thi đấu\n" + Util.numberToMoney(pl.goldChallenge) + " vàng");
        
        if (pl.levelWoodChest > 0) {
            menu.add("Nhận\nthưởng\nRương Cấp\n" + pl.levelWoodChest);
        }
        
        menu.add("Về\nĐại Hội\nVõ Thuật");
        menu.add("Từ chối");

        this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                "Đại hội võ thuật lần thứ 23\nDiễn ra bất kể ngày đêm, ngày nghỉ, ngày lễ\nPhần thưởng vô cùng quý giá\nNhanh chóng tham gia nào",
                menu.toArray(new String[0]));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.DAI_HOI_VO_THUAT -> WorldMartialArtsTournamentService.confirm(this, player, select);
            case ConstMap.DAI_HOI_VO_THUAT_129 -> handleDHVT23Confirm(player, select);
        }
    }

    private void handleDHVT23Confirm(Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();
        if (indexMenu == ConstNpc.BASE_MENU) {
            handleDHVT23BaseMenu(player, select);
        } else if (indexMenu == MENU_REWARD_CHEST) {
            handleDHVT23RewardConfirm(player, select);
        }
    }

    private void handleDHVT23BaseMenu(Player player, int select) {
        boolean hasChest = player.levelWoodChest > 0;

        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.NPC_DHVT23);
            case 1 -> tryChallengeDHVT23(player, true);
            case 2 -> tryChallengeDHVT23(player, false);
            case 3 -> {
                if (hasChest) {
                    createOtherMenu(player, MENU_REWARD_CHEST,
                            "Phần thưởng của bạn đang ở cấp " + player.levelWoodChest + " / " + MAX_LEVEL_CHEST
                                    + "\nMỗi ngày chỉ được nhận phần thưởng 1 lần\nBạn có chắc sẽ nhận phần thưởng ngay bây giờ?",
                            "OK", "Từ chối");
                } else {
                    goBackToMainDHVT(player);
                }
            }
            case 4 -> {
                if (hasChest) goBackToMainDHVT(player);
            }
        }
    }

    private void tryChallengeDHVT23(Player player, boolean useRuby) {
        if (player.levelWoodChest >= MAX_LEVEL_CHEST) {
            Service.gI().sendThongBao(player, "Bạn đã vô địch giải. Vui lòng chờ đến ngày mai");
            return;
        }

        if (!InventoryService.gI().finditemWoodChest(player)) {
            Service.gI().sendThongBao(player, "Hãy mở rương báu vật trước");
            return;
        }

        long cost = useRuby ? player.rubyChallenge : player.goldChallenge;
        boolean canAfford = useRuby ? (player.inventory.gem >= cost) : (player.inventory.gold >= cost);

        if (!canAfford) {
            String unit = useRuby ? "ngọc" : "vàng";
            long balance = useRuby ? player.inventory.gem : player.inventory.gold;
            Service.gI().sendThongBao(player, "Bạn không đủ " + unit + ", còn thiếu " + Util.numberToMoney(cost - balance) + " " + unit + " nữa");
            return;
        }

        if (useRuby) player.inventory.gem -= cost;
        else player.inventory.gold -= cost;

        The23rdMartialArtCongressService.gI().startChallenge(player);
        afterChallengeDHVT23Success(player);
    }

    private void afterChallengeDHVT23Success(Player player) {
        PlayerService.gI().sendInfoHpMpMoney(player);
        player.goldChallenge *= 2;
        player.rubyChallenge += 2;
    }

    private void handleDHVT23RewardConfirm(Player player, int select) {
        if (select != 0) return;

        if (!InventoryService.gI().finditemWoodChest(player)) {
            Service.gI().sendThongBao(player, "Hãy mở rương báu vật trước");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            this.npcChat(player, "Hành trang đã đầy, cần một ô trống để nhận vật phẩm");
            return;
        }

        Item chest = ItemService.gI().createNewItem((short) ID_RUONG_GO);
        chest.itemOptions.add(new Item.ItemOption(72, player.levelWoodChest));
        chest.itemOptions.add(new Item.ItemOption(30, 0));
        chest.createTime = System.currentTimeMillis();

        InventoryService.gI().addItemBag(player, chest);
        InventoryService.gI().sendItemBags(player);

        player.levelWoodChest = 0;
        player.lastTimeRewardWoodChest = System.currentTimeMillis();

        NpcService.gI().createMenuConMeo(player, -1, -1,
                "Bạn nhận được\n|1|Rương Gỗ\n|2|Giấu bên trong nhiều vật phẩm quý giá", "OK");
    }

    private void goBackToMainDHVT(Player player) {
        ChangeMapService.gI().changeMapNonSpaceship(player, ConstMap.DAI_HOI_VO_THUAT, player.location.x, Y_LANDING_DHVT);
    }
}