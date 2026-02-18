package boss.The23rdMartialArtCongress;


import boss.BossID;
import boss.BossesData;
import player.Player;

import static consts.BossType.PHOBAN;

public class Xinbato extends The23rdMartialArtCongress {

    public Xinbato(Player player) throws Exception {
        super(PHOBAN, BossID.XINBATO, BossesData.XINBATO);
        this.playerAtt = player;
    }
}
