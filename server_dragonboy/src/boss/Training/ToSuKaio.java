package boss.Training;


import services.TrainingService;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import services.map.ChangeMapService;
import player.Player;
import services.Service;
import utils.Util;

import static consts.BossType.PHOBAN;

public class ToSuKaio extends TrainingBoss {

    private long lastTimeLuyenTap;

    public ToSuKaio(Player player) throws Exception {
        super(PHOBAN, BossID.TO_SU_KAIO, BossesData.TO_SU_KAIO);
        this.playerAtt = player;
    }

    @Override
    public void joinMap() {
        if (playerAtt.zone != null) {
            this.zone = playerAtt.zone;
            ChangeMapService.gI().changeMap(this, this.zone, playerAtt.location.x, playerAtt.location.y);
            this.changeStatus(BossStatus.CHAT_S);
            lastTimeLuyenTap = System.currentTimeMillis();
        }
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        return 0;
    }

    @Override
    public void active() {
        if (playerAtt.location != null && playerAtt != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
            if (Util.canDoWithTime(lastTimeLuyenTap, 10000)) {
                Service.gI().addSMTN(playerAtt, (byte) 2, TrainingService.gI().getTnsmMoiPhut(playerAtt) / 6, false);
                lastTimeLuyenTap = System.currentTimeMillis();
            }
        } else {
            this.leaveMap();
        }
    }
}
