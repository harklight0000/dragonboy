package services.shenron;

import server.Maintenance;
import utils.Functions;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Shenron_Manager implements Runnable {

    private static final List<Shenron_Event> list = new ArrayList<>();
    private static Shenron_Manager instance;
    private long lastUpdate;

    ;

    public static Shenron_Manager gI() {
        if (instance == null) {
            instance = new Shenron_Manager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long start = System.currentTimeMillis();
                update();
                long timeUpdate = System.currentTimeMillis() - start;
                Functions.sleep(Math.max(1000 - timeUpdate, 10));
            } catch (Exception ex) {
            }
        }
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            List<Shenron_Event> listCopy = new ArrayList<>(list);

            for (Shenron_Event se : listCopy) {
                try {
                    se.update();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            listCopy.clear();
        }
    }

    public void add(Shenron_Event se) {
        list.add(se);
    }

    public void remove(Shenron_Event se) {
        list.remove(se);
    }

}
