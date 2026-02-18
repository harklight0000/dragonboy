package services;

import player.Player;
import player.badges.BadgesData;

public class BadgesService {

    public static void turnOnBadges(Player player, int id) {
        if (player.dataBadges != null) {
            for (BadgesData data : player.dataBadges) {
                if (data.idBadGes == id) {
                    data.isUse = true;
                } else {
                    data.isUse = false;
                }
            }
        }
    }

}
