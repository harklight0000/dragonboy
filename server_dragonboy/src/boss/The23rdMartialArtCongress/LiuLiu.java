package boss.The23rdMartialArtCongress;


import boss.BossID;
import boss.BossesData;
import player.Player;

import static consts.BossType.PHOBAN;

public class LiuLiu extends The23rdMartialArtCongress {

    public LiuLiu(Player player) throws Exception {
        super(PHOBAN, BossID.LIU_LIU, BossesData.LIU_LIU);
        this.playerAtt = player;
    }
}
