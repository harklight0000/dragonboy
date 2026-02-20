package npc.list;

import dungeon.DestronGas;
import services.dungeon.DestronGasService;
import clan.Clan;
import clan.ClanMember;
import consts.ConstNpc;
import consts.ConstMap;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.func.Input;
import utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

import static npc.NpcFactory.PLAYERID_OBJECT;

public class MrPoPo extends Npc {

    private static final int MENU_JOIN_DESTRON_GAS = 2;
    private static final int MIN_JOIN_DATE = 2;
    private static final int MIN_CLAN_MEMBERS = 5;

    public MrPoPo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player) || this.mapId != ConstMap.LANG_ARU) return;

        List<String> menu = new ArrayList<>();
        menu.add("Thông tin\nChi tiết");
        menu.add("Top 100\nBang hội");

        if (player.clan != null) {
            menu.add("Thành tích\nBang");
        }

        menu.add("OK");
        menu.add("Từ chối");

        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Thượng Đế vừa phát hiện ra 1 loại khí đang âm thầm\nhủy diệt mọi mầm sống trên Trái Đất,\nnó được gọi là Destron Gas.\nTa sẽ đưa các cậu đến nơi ấy, các cậu đã sẵn sàng chưa?",
                menu.toArray(String[]::new));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || this.mapId != ConstMap.LANG_ARU) return;

        int indexMenu = player.idMark.getIndexMenu();
        if (player.idMark.isBaseMenu()) {
            handleBaseMenu(player, select);
            return;
        }

        if (indexMenu == MENU_JOIN_DESTRON_GAS) {
            handleConfirmJoin(player, select);
        }
    }

    private void handleBaseMenu(Player player, int select) {
        int actualSelect = player.clan != null ? select : (select >= 2 ? select + 1 : select);

        switch (actualSelect) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_KHI_GAS_HUY_DIET);
            case 3 -> prepareDestronGas(player);
        }
    }

    private void prepareDestronGas(Player player) {
        Clan clan = player.clan;
        if (clan == null) return;

        ClanMember cm = clan.getClanMember((int) player.id);
        if (cm == null) return;

        if (player.clanMember.getNumDateFromJoinTimeToToday() < MIN_JOIN_DATE) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Gia nhập bang hội trên " + MIN_JOIN_DATE + " ngày mới được tham gia");
            return;
        }

        if (clan.KhiGasHuyDiet != null) {
            String msg = String.format("Bang hội của cậu đang tham gia Destron Gas cấp độ %d\ncậu có muốn đi cùng họ không ? (%s trước)",
                    clan.KhiGasHuyDiet.level, TimeUtil.convertTimeNow(clan.KhiGasHuyDiet.getLastTimeOpen()));
            createOtherMenu(player, MENU_JOIN_DESTRON_GAS, msg, "Đồng ý", "Từ chối");
            return;
        }

        if (!clan.isLeader(player)) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Chức năng chỉ dành cho bang chủ");
            return;
        }

        if (clan.members.size() < MIN_CLAN_MEMBERS) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Bang hội phải có ít nhất " + MIN_CLAN_MEMBERS + " thành viên mới có thể tham gia");
            return;
        }

        Input.gI().createFormChooseLevelKGHD(player);
    }

    private void handleConfirmJoin(Player player, int select) {
        if (select != 0 || player.clan == null) return;

        byte level = (player.clan.KhiGasHuyDiet == null) 
                ? Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))) 
                : 0;
        
        DestronGasService.gI().openKhiGasHuyDiet(player, level);
    }
}