package dungeon;

import lombok.Data;
import map.MaBuHold;
import services.map.ChangeMapService;
import services.map.MapService;
import map.Zone;
import player.Player;
import server.Maintenance;
import utils.Functions;
import utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Data
public final class MajinBuu14H implements Runnable {

    public static final int AVAILABLE = 7;
    public final List<Zone> zones;
    public int id;

    public MajinBuu14H(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
        this.init();
    }

    public void init() {
        Executors.newSingleThreadExecutor().submit(this, "MajinBuu 14H - Id : " + id);
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - startTime), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (!TimeUtil.isMabu14HOpen()) {
            finish();
            return;
        }
        for (int j = zones.size() - 1; j >= 0; j--) {
            Zone zone = zones.get(j);
            for (MaBuHold hold : zone.maBuHolds) {
                if (hold.player != null && hold.player.maBuHold == null && hold.player.zone != null) {
                    hold.player = null;
                }
            }
        }
    }

    public MaBuHold getMaBuHold() {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == 128) {
                for (MaBuHold hold : zone.maBuHolds) {
                    if (hold.player == null) {
                        return hold;
                    }
                }
            }
        }
        return null;
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void finish() {
        for (int j = zones.size() - 1; j >= 0; j--) {
            Zone zone = zones.get(j);
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOut(pl);
                }
            }
        }
    }

    private void kickOut(Player player) {
        if (MapService.gI().isMapMabu2H(player.zone.map.mapId) && !player.isAdmin()) {
            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 336);
        }
    }

}
