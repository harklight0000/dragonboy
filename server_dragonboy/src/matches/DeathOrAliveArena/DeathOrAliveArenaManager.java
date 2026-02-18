package matches.DeathOrAliveArena;

import lombok.NonNull;
import map.Zone;
import server.Maintenance;
import utils.Functions;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class DeathOrAliveArenaManager implements Runnable {

    private static final List<DeathOrAliveArenaSession> list = new ArrayList<>();
    private static DeathOrAliveArenaManager instance;
    private volatile long lastUpdate;

    public static DeathOrAliveArenaManager gI() {
        if (instance == null) {
            instance = new DeathOrAliveArenaManager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long start = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - start), 10));
            } catch (Exception ex) {
            }
        }
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            for (int i = list.size() - 1; i >= 0; i--) {
                if (i < list.size()) {
                    list.get(i).update();
                }
            }
        }
    }

    public void add(DeathOrAliveArenaSession vdst) {
        list.add(vdst);
    }

    public void remove(DeathOrAliveArenaSession vdst) {
        list.remove(vdst);
    }

    public DeathOrAliveArenaSession getVDST(@NonNull Zone zone) {
        for (DeathOrAliveArenaSession vdst : list) {
            if (vdst.getZone().equals(zone)) {
                return vdst;
            }
        }
        return null;
    }
}
