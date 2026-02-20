package npc.list;

import clan.Clan;
import clan.ClanMember;
import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstTask;
import consts.ConstMap;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.ClanService;
import server.Client;
import services.Service;
import services.TaskService;
import services.func.Input;
import utils.Functions;
import utils.Util;
import java.util.ArrayList;

public class DrDrief extends Npc {

    private static final int TASK_SAVE_KID = 7;
    private static final int MENU_CLAN_FUNCTIONS = 1;

    public DrDrief(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player pl) {
        if (!canOpenNpc(pl)) return;

        switch (this.mapId) {
            case ConstMap.SIEU_THI -> openMenuSieuThi(pl);
            case ConstMap.LANH_DIA_BANG_HOI -> openMenuBangHoi(pl);
            default -> openMenuHanhTinh(pl);
        }
    }

    private void openMenuSieuThi(Player pl) {
        String destination = switch (pl.gender) {
            case ConstPlayer.TRAI_DAT -> "Trái Đất";
            case ConstPlayer.NAMEC -> "Namếc";
            default -> "Xayda";
        };
        this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?",
                "Đến\n" + destination);
    }

    private void openMenuBangHoi(Player pl) {
        ArrayList<String> menu = new ArrayList<>();
        Clan clan = pl.clan;
        if (clan != null) {
            if (clan.isLeader(pl)) menu.add("Chức năng\nbang hội");
            menu.add("Nhiệm vụ Bang\n[" + pl.playerTask.clanTask.leftTask + "/" + ConstTask.MAX_CLAN_TASK + "]");
        }
        menu.add("Đảo Kame");
        menu.add("Từ chối");
        this.createOtherMenu(pl, ConstNpc.BASE_MENU, "Tôi có thể giúp gì cho bang hội của bạn ?", menu.toArray(String[]::new));
    }

    private void openMenuHanhTinh(Player pl) {
        if (TaskService.gI().checkDoneTaskTalkNpc(pl, this)) return;

        if (pl.playerTask.taskMain.id == TASK_SAVE_KID) {
            NpcService.gI().createTutorial(pl, this.avartar, "Hãy lên đường cứu đứa bé nhà tôi\nChắc bây giờ nó đang sợ hãi lắm rồi");
            return;
        }
        this.createOtherMenu(pl, ConstNpc.BASE_MENU,
                "Tàu Vũ Trụ của ta có thể đưa cậu đến hành tinh khác chỉ trong 3 giây. Cậu muốn đi đâu?",
                "Đến\nNamếc", "Đến\nXayda", "Siêu thị");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        switch (this.mapId) {
            case ConstMap.SIEU_THI -> {
                int destMap = player.gender + ConstMap.TRAM_TAU_VU_TRU; 
                ChangeMapService.gI().changeMapBySpaceShip(player, destMap, -1, -1);
            }
            case ConstMap.LANH_DIA_BANG_HOI -> confirmMenuBangHoi(player, select);
            default -> confirmMenuHanhTinh(player, select);
        }
    }

    private void confirmMenuBangHoi(Player player, int select) {
        int indexMenu = player.idMark.getIndexMenu();
        switch (indexMenu) {
            case ConstNpc.BASE_MENU -> handleBaseMenuBangHoi(player, select);
            case MENU_CLAN_FUNCTIONS -> handleClanFunctions(player, select);
            case ConstNpc.MENU_CLAN_UP -> handleClanUpgrade(player, select);
            case ConstNpc.MENU_CLAN_TASK -> handleClanTask(player, select);
            case ConstNpc.MENU_CLAN_TASK_REMOVE -> handleClanTaskRemove(player, select);
        }
    }

    private void handleBaseMenuBangHoi(Player player, int select) {
        Clan clan = player.clan;
        if (clan == null) return;

        if (clan.isLeader(player)) {
            switch (select) {
                case 0 -> createOtherMenu(player, MENU_CLAN_FUNCTIONS, "Tôi có thể giúp gì cho bang hội của bạn ?", "Đổi tên\ntên bang\nviết tắt", "Chọn ngẫu nhiên tên bang viết tắt", "Nâng cấp Bang hội", "Đóng");
                case 1 -> processClanTaskStatus(player);
                case 2 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.DAO_KAME, -1, -1);
            }
        } else {
            switch (select) {
                case 0 -> processClanTaskStatus(player);
                case 1 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.DAO_KAME, -1, -1);
            }
        }
    }

    private void processClanTaskStatus(Player player) {
        if (player.playerTask.clanTask.template != null) {
            if (player.playerTask.clanTask.isDone()) {
                createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ đã hoàn thành, hãy nhận " + ((player.playerTask.clanTask.level + 1) * 10) + " capsule bang", "Nhận\nthưởng", "Đóng");
            } else {
                createOtherMenu(player, ConstNpc.MENU_CLAN_TASK, "Nhiệm vụ hiện tại: " + player.playerTask.clanTask.getName() + ". Đã hạ được " + player.playerTask.clanTask.count, "OK", "Hủy bỏ\nNhiệm vụ\nnày");
            }
        } else {
            TaskService.gI().changeClanTask(this, player, (byte) Util.nextInt(5));
        }
    }

    private void handleClanFunctions(Player player, int select) {
        Clan clan = player.clan;
        if (clan == null || !clan.isLeader(player)) return;

        switch (select) {
            case 0 -> Input.gI().createFormBangHoi(player);
            case 1 -> {
                if (clan.canUpdateClan(player)) {
                    String tag = Functions.generateRandomCharacters(Util.nextInt(2, 4));
                    clan.name2 = tag;
                    clan.update();
                    Service.gI().sendThongBao(player, "[" + tag + "] OK");
                }
            }
            case 2 -> handleUpgradeMenu(player, clan);
        }
    }

    private void handleUpgradeMenu(Player player, Clan clan) {
        if (clan.level > 10) {
            Service.gI().sendThongBao(player, "Đang ở cấp độ cao nhất.");
            return;
        }
        String npcSay = "Cần " + Util.formatNumber(ClanService.gI().capsule(clan)) + " capsule bang [đang có " + Util.formatNumber(clan.capsuleClan) + " capsule bang] để nâng cấp bang hội lên cấp " + (clan.level + 1);
        npcSay += "\n+1 tối đa số lượng thành viên";
        if (clan.level > 1) npcSay += "\n+1 ô trống tối đa rương bang.";
        npcSay += "\n+Mở bán bùa bang cấp " + (clan.level + 1);
        createOtherMenu(player, ConstNpc.MENU_CLAN_UP, npcSay, "Đồng ý", "Từ chối");
    }

    private void handleClanUpgrade(Player player, int select) {
        if (select != 0) return;
        Clan clan = player.clan;
        if (clan == null || !clan.isLeader(player)) return;

        int required = ClanService.gI().capsule(clan);
        if (clan.capsuleClan >= required) {
            clan.capsuleClan -= required;
            clan.level++;
            clan.maxMember++;
            Service.gI().sendThongBao(player, "Chúc mừng bang hội của bạn đã lên cấp " + (clan.level));
            refreshClanData(player);
        } else {
            Service.gI().sendThongBao(player, "Không đủ capsule bang, cần " + Util.formatNumber(required - clan.capsuleClan) + " capsule bang nữa.");
        }
    }

    private void refreshClanData(Player player) {
        for (ClanMember cm : player.clan.getMembers()) {
            Player member = Client.gI().getPlayer(cm.id);
            if (member != null) ClanService.gI().sendMyClan(player);
        }
    }

    private void handleClanTask(Player player, int select) {
        if (player.playerTask.clanTask.template == null) return;
        switch (select) {
            case 0 -> { if (player.playerTask.clanTask.isDone()) TaskService.gI().payClanTask(player); }
            case 1 -> {
                if (!player.playerTask.clanTask.isDone()) {
                    createOtherMenu(player, ConstNpc.MENU_CLAN_TASK_REMOVE, "Bạn có chắc muốn hủy nhiệm vụ này?\nNếu hủy nhiệm vụ bạn sẽ mất 1 lượt nhiệm vụ trong ngày.", "Đồng ý", "Từ chối");
                }
            }
        }
    }

    private void handleClanTaskRemove(Player player, int select) {
        if (select == 0 && player.playerTask.clanTask.template != null && !player.playerTask.clanTask.isDone()) {
            TaskService.gI().removeClanTask(player);
        }
    }

    private void confirmMenuHanhTinh(Player player, int select) {
        if (!player.idMark.isBaseMenu()) return;
        switch (select) {
            case 0 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.TRAM_TAU_VU_TRU_25, -1, -1);
            case 1 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.TRAM_TAU_VU_TRU_26, -1, -1);
            case 2 -> ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.SIEU_THI, -1, -1);
        }
    }
}