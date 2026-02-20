package npc.list;

import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstMap;
import services.map.ChangeMapService;
import services.map.NpcService;
import npc.Npc;
import player.Player;
import services.RewardService;
import services.Service;
import services.TaskService;
import services.func.Input;
import shop.ShopService;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;
import java.util.ArrayList;
import player.skill.LearnSkillService;

public class VuaVegeta extends Npc {

    private static final int MENU_XAC_NHAN_HUY_HOC = 13;
    private static final int MENU_XAC_NHAN_GIAI_TAN = 3;
    private static final int TIME_UNIT_FOR_GEM_CALC = 600_000;
    private static final int Y_LANDING_CLAN_AREA = 432;

    public VuaVegeta(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player) || TaskService.gI().checkDoneTaskTalkNpc(player, this)) return;

        if (player.gender != ConstPlayer.XAYDA) {
            NpcService.gI().createTutorial(player, tempId, avartar, "Con hãy về hành tinh của mình mà thể hiện");
            return;
        }

        if (player.canReward) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chào con...", "Giao\nLân con");
            return;
        }

        openDefaultBaseMenu(player);
    }

    private void openDefaultBaseMenu(Player player) {
        ArrayList<String> menu = new ArrayList<>();
        menu.add("Nhiệm vụ");
        menu.add("Học\nKỹ năng");

        if (player.clan != null) {
            menu.add("Về khu\nvực bang");
            if (player.clan.isLeader(player)) {
                menu.add("Giải tán\nBang hội");
            }
        }

        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Chào con, ta rất vui khi gặp được con\nCon muốn làm gì nào ?", menu.toArray(String[]::new));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        if (player.canReward) {
            RewardService.gI().rewardLancon(player);
            return;
        }

        int indexMenu = player.idMark.getIndexMenu();
        switch (indexMenu) {
            case ConstNpc.BASE_MENU -> handleBaseMenu(player, select);
            case ConstNpc.MENU_HOC_KY_NANG -> handleHocKyNangMenu(player, select);
            case MENU_XAC_NHAN_HUY_HOC -> handleConfirmCancelLearn(player, select);
            case MENU_XAC_NHAN_GIAI_TAN -> handleConfirmClanDissolution(player, select);
        }
    }

    private void handleBaseMenu(Player player, int select) {
        switch (select) {
            case 0 -> NpcService.gI().createTutorial(player, tempId, avartar,
                    player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
            case 1 -> handleLearnSkillStep(player);
            case 2 -> {
                if (player.clan != null) {
                    ChangeMapService.gI().changeMapNonSpaceship(player, ConstMap.LANH_DIA_BANG_HOI, Util.nextInt(100, 200), Y_LANDING_CLAN_AREA);
                }
            }
            case 3 -> {
                if (player.clan != null && player.clan.isLeader(player)) {
                    createOtherMenu(player, MENU_XAC_NHAN_GIAI_TAN, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                }
            }
        }
    }

    private void handleLearnSkillStep(Player player) {
        if (player.LearnSkill.Time == -1) {
            ShopService.gI().opendShop(player, "QUY_LAO", false);
            return;
        }

        if (player.LearnSkill.Time <= System.currentTimeMillis()) {
            LearnSkillService.gI().finishLearnSkill(player);
            return;
        }

        long timeRemaining = player.LearnSkill.Time - System.currentTimeMillis();
        int ngocCost = calculateNgocCost(timeRemaining);
        byte levelTarget = LearnSkillService.gI().getLevelFromItem((int) player.LearnSkill.ItemTemplateSkillId);
        String skillName = SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name;

        createOtherMenu(player, ConstNpc.MENU_HOC_KY_NANG,
                "Con đang học kỹ năng\n" + skillName + " cấp " + levelTarget + "\nThời gian còn lại " + TimeUtil.getTime(timeRemaining),
                "Học Cấp tốc\n" + ngocCost + " ngọc", "Huỷ", "Bỏ qua");
    }

    private void handleHocKyNangMenu(Player player, int select) {
        switch (select) {
            case 0 -> {
                long timeRemaining = player.LearnSkill.Time - System.currentTimeMillis();
                int ngocCost = calculateNgocCost(timeRemaining);

                if (player.inventory.gem < ngocCost) {
                    Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                    return;
                }

                player.inventory.subGem(ngocCost);
                LearnSkillService.gI().finishLearnSkill(player);
            }
            case 1 -> createOtherMenu(player, MENU_XAC_NHAN_HUY_HOC, "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
        }
    }

    private void handleConfirmCancelLearn(Player player, int select) {
        if (select == 0) {
            LearnSkillService.gI().cancelLearn(player);
        }
    }

    private void handleConfirmClanDissolution(Player player, int select) {
        if (select == 0 && player.clan != null && player.clan.isLeader(player)) {
            Input.gI().createFormGiaiTanBangHoi(player);
        }
    }

    private int calculateNgocCost(long timeRemaining) {
        return (timeRemaining / TIME_UNIT_FOR_GEM_CALC >= 2) ? (5 + (int) (timeRemaining / TIME_UNIT_FOR_GEM_CALC)) : 5;
    }
}