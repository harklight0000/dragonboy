package npc.list;

import consts.ConstNpc;
import java.util.HashSet;
import player.PlayerDAO;
import npc.Npc;
import player.Player;
import services.PlayerService;
import services.player.InventoryService;
import services.Service;
import services.TaskService;
import services.func.Input;

public class LyTieuNuong extends Npc {

    public LyTieuNuong(int mapid, int status, int cx, int cy, int tempid, int avartar) {
        super(mapid, status, cx, cy, tempid, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player) && !TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "|0| Cậu muốn làm gì?",
                    "Mua Thành Viên", "Đổi Thỏi Vàng");
        }
    }

    
    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player) && player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> {
                    if(player.getSession().actived) {
                        npcChat(player, "Bạn đã mở thành viên!");
                        return;
                    }
                    boolean result = PlayerService.gI().setActive(player);
                    if(result)  npcChat(player, "Đã Mua thành viên thành công!");
                    else npcChat(player, "Bạn cần " + PlayerService.gI().constMoThanhVien  + " vnd để mở thành viên!");
                }
                case 1 -> {
                    Input.gI().createFormTradeGold(player);
                }
            }
        }
    }
}