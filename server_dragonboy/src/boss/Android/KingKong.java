package boss.Android;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

import java.util.Random;

public class KingKong extends Boss {

    private long st;

    public KingKong() throws Exception {
        super(BossID.KING_KONG, BossesData.KING_KONG);
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, Util.nextInt(20000, 30001),
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        if (Util.isTrue(80, 100)) {
            int[] items = Util.isTrue(50, 100) ? new int[]{18, 19, 20} : new int[]{18, 19, 20};
            int randomItem = items[new Random().nextInt(items.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, randomItem, 1,
                    this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }
}
