package server;

import commands.Shell;
import services.NgocRongNamecService;
import boss.BossManager.*;
import kygui.ConsignShopManager;
import matches.SuperRank.SuperRankManager;
import matches.DeathOrAliveArena.DeathOrAliveArenaManager;
import matches.The23rdMartialArtCongress.The23rdMartialArtCongressManager;
import matches.WorldMartialArtsTournamen.WorldMartialArtsTournamentManager;
import network.MessageSendCollect;
import network.MyKeyHandler;
import network.MySession;
import network.Network;
import network.inetwork.ISession;
import network.inetwork.ISessionAcceptHandler;
import services.ClanService;
import npc.NpcFactory;
import services.shenron.Shenron_Manager;
import config.Config;
import logger.NLogger;
import utils.TimeUtil;

import java.util.HashMap;
import java.util.Map;

public class DragonBoy {

    public static final Map<Object, Object> CLIENTS = new HashMap<>();
    public static String timeStart;
    public static String NAME_SERVER = Config.gI().getSV_NAME();// Tên Máy Chủ
    public static String DOMAIN = Config.gI().getSV_DOMAIN(); // Domain Truy Cập
    public static String NAME = Config.gI().getSV_NAME(); // Name Khi Vào Giao Diện Game
    public static String IP = Config.gI().getSV_IP(); // ip mac dinh
    public static int PORT = Config.gI().getSV_PORT(); // Port
    public static int EVENT_SERVER = 0;
    public static boolean isRunning;
    private static DragonBoy instance;
    public static DragonBoy gI() {
        if (instance == null) {
            instance = new DragonBoy();
        }
        return instance;
    }

    public static void main(String[] args) {
        try {
           // NLogger.logInformation("Conne");

            DragonBoy.gI().init();
            DragonBoy.gI().run();
        }catch (Throwable  e) {
            NLogger.logCritical( e, "Error initializing app context");
            System.exit(0);
        }

    }

    public void init() {

        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
        Shell.configureLogger();
        GameData.gI().load();
        NpcFactory.init();
        HistoryTransactionDAO.deleteHistory();
        
    }

    public void run() throws Exception {
        isRunning = true;
        activeServerSocket();
        new Thread(NgocRongNamecService.gI(), "Update NRNM").start();
        new Thread(SuperRankManager.gI(), "Update Super Rank").start();
        new Thread(The23rdMartialArtCongressManager.gI(), "Update DHVT23").start();
        new Thread(DeathOrAliveArenaManager.gI(), "Update Võ Đài Sinh Tử").start();
        new Thread(WorldMartialArtsTournamentManager.gI(), "Update WMAT").start();
        new Thread(Shenron_Manager.gI(), "Update Shenron").start();
        BossManager.gI().loadBoss();
        GameData.MAPS.forEach(map.Map::initBoss);
        new Thread(BossManager.gI(), "Update boss").start();
        new Thread(YardartManager.gI(), "Update yardart boss").start();
        new Thread(FinalBossManager.gI(), "Update final boss").start();
        new Thread(SkillSummonedManager.gI(), "Update Skill-summoned boss").start();
        new Thread(BrolyManager.gI(), "Update broly boss").start();
        new Thread(OtherBossManager.gI(), "Update other boss").start();
        new Thread(RedRibbonHQManager.gI(), "Update reb ribbon hq boss").start();
        new Thread(TreasureUnderSeaManager.gI(), "Update treasure under sea boss").start();
        new Thread(SnakeWayManager.gI(), "Update snake way boss").start();
        new Thread(GasDestroyManager.gI(), "Update gas destroy boss").start();
        new Thread(new Shell(), "Shell").start();

    }

    public void activeServerSocket() throws Exception {
            Network.gI().init().setAcceptHandler(new ISessionAcceptHandler() {
                        @Override
                        public void sessionInit(ISession is) {
                            if (!canConnectWithIp(is.getIP())) {
                                is.disconnect();
                                return;
                            }
                            is.setMessageHandler(Controller.gI())
                                    .setSendCollect(new MessageSendCollect())
                                    .setKeyHandler(new MyKeyHandler())
                                    .startCollect();
                        }

                        @Override
                        public void sessionDisconnect(ISession session) {
                            MySession mySession = (MySession) session;
                            if (mySession.player == null) {
                                return;
                            }
                            NLogger.logInformation(mySession.player.name + " left the game");

                            Client.gI().kickSession((MySession) session);
                            disconnect((MySession) session);

                        }
                    }).setTypeSessioClone(MySession.class)
                    .setDoSomeThingWhenClose(() -> {
                        NLogger.logWarning("SERVER CLOSE");
                        System.exit(0);
                    })
                    .start(PORT);

    }

    private boolean canConnectWithIp(String ipAddress) {
        Object o = CLIENTS.get(ipAddress);
        if (o == null) {
            CLIENTS.put(ipAddress, 1);
            return true;
        } else {
            int n = Integer.parseInt(String.valueOf(o));
            if (n < GameData.MAX_PER_IP) {
                n++;
                CLIENTS.put(ipAddress, n);
                return true;
            } else {
                return false;
            }
        }
    }

    public void disconnect(MySession session) {
        Object o = CLIENTS.get(session.getIP());
        if (o != null) {
            int n = Integer.parseInt(String.valueOf(o));
            n--;
            if (n < 0) {
                n = 0;
            }
            CLIENTS.put(session.getIP(), n);
        }
    }

    public void close() {
        isRunning = false;
        try { ClanService.gI().close(); }
        catch (Exception e) { NLogger.logWarning("Error to save clan!"); }

        try { ConsignShopManager.gI().save(); }
        catch (Exception e) { NLogger.logWarning("Error to save consign shop!"); }

        try { Client.gI().close(); }
        catch (Exception e) { NLogger.logWarning("Error to close clients!"); }

        NLogger.logInformation("SUCCESSFULLY MAINTENANCE!");
        System.exit(0);
    }

}