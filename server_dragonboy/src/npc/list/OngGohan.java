package npc.list;

import consts.ConstNpc;
import npc.Npc;
import player.Player;
import services.TaskService;
import services.func.Input;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|0| Con cố gắng học thành tài, đừng lo lắng cho ta",
                        "Giftcode",
                        "Đổi Mật Khẩu");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> Input.gI().createFormGiftCode(player);
                    case 1 -> Input.gI().createFormChangePassword(player);
                }
            }
        }
    }
}