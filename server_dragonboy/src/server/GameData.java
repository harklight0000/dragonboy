package server;

import clan.Clan;
import consts.ConstMap;
import database.SqlFetcher;
import player.intrinsic.Intrinsic;
import item.Template.*;
import map.Zone;
import npc.NonInteractiveNPC;
import npc.Npc;
import player.badges.BagesTemplate;
import shop.Shop;
import player.skill.NClass;
import task.*;
import config.Config;
import logger.NLogger;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class GameData {

    public static final List<map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final List<ArrHead2Frames> ARR_HEAD_2_FRAMES = new ArrayList<>();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<ClanTaskTemplate> CLAN_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<AchievementTemplate> ACHIEVEMENT_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<BgItem> BG_ITEMS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static final List<BadgesTaskTemplate> TASKS_BADGES_TEMPLATE = new ArrayList<>();
    public static final List<BagesTemplate> BAGES_TEMPLATES = new ArrayList<>();
    public static final short[][] trangBiKichHoat = {{0, 6, 21, 27}, {1, 7, 22, 28}, {2, 8, 23, 29}};
    public static final short[] aotd = {138, 139, 230, 231, 232, 233, 555};
    public static final short[] quantd = {142, 143, 242, 243, 244, 245, 556};
    public static final short[] gangtd = {146, 147, 254, 255, 256, 257, 562};
    public static final short[] giaytd = {150, 151, 266, 267, 268, 269, 563};
    public static final short[] aoxd = {170, 171, 238, 239, 240, 241, 559};
    public static final short[] quanxd = {174, 175, 250, 251, 252, 253, 560};
    public static final short[] gangxd = {178, 179, 262, 263, 264, 265, 566};
    public static final short[] giayxd = {182, 183, 274, 275, 276, 277, 567};
    public static final short[] aonm = {154, 155, 234, 235, 236, 237, 557};
    public static final short[] quannm = {158, 159, 246, 247, 248, 249, 558};
    public static final short[] gangnm = {162, 163, 258, 259, 260, 261, 564};
    public static final short[] giaynm = {166, 167, 270, 271, 272, 273, 565};
    public static final short[] radaSKHVip = {186, 187, 278, 279, 280, 281, 561};
    public static final short[][][] doSKHVip = {{aotd, quantd, gangtd, giaytd}, {aonm, quannm, gangnm, giaynm},
            {aoxd, quanxd, gangxd, giayxd}};
    public static final int[][][] LIST_ITEM_CLOTHES = {
            {{0, 33, 3, 34, 136, 137, 138, 139, 230, 231, 232, 233, 555}, {6, 35, 9, 36, 140, 141, 142, 143, 242, 243, 244, 245, 556}, {21, 24, 37, 38, 144, 145, 146, 147, 254, 255, 256, 257, 562}, {27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269, 563}, {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}},
            {{1, 41, 4, 42, 152, 153, 154, 155, 234, 235, 236, 237, 557}, {7, 43, 10, 44, 156, 157, 158, 159, 246, 247, 248, 249, 558}, {22, 46, 25, 45, 160, 161, 162, 163, 258, 259, 260, 261, 564}, {28, 47, 31, 48, 164, 165, 166, 167, 270, 271, 272, 273, 565}, {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}},
            {{2, 49, 5, 50, 168, 169, 170, 171, 238, 239, 240, 241, 559}, {8, 51, 11, 52, 172, 173, 174, 175, 250, 251, 252, 253, 560}, {23, 53, 26, 54, 176, 177, 178, 179, 262, 263, 264, 265, 566}, {29, 55, 32, 56, 180, 181, 182, 183, 274, 275, 276, 277, 567}, {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}}
    };
    public static final short[] Ao_TraiDat = {0, 33, 3, 34, 136, 137, 138, 139, 230, 231, 232, 233, 555, 650};
    public static final short[] Quan_TraiDat = {6, 35, 9, 36, 140, 141, 142, 143, 242, 243, 244, 245, 556, 651};
    public static final short[] Gang_TraiDat = {21, 24, 37, 38, 144, 145, 146, 147, 254, 255, 256, 257, 562, 657};
    public static final short[] Giay_TraiDat = {27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269, 563, 658};
    public static final short[] Ao_Namek = {1, 41, 4, 42, 152, 153, 154, 155, 234, 235, 236, 237, 557, 652};
    public static final short[] Quan_Namek = {7, 43, 10, 44, 156, 157, 158, 159, 246, 247, 248, 249, 558, 653};
    public static final short[] Gang_Namek = {22, 46, 25, 45, 160, 161, 162, 163, 258, 259, 260, 261, 564, 659};
    public static final short[] Giay_Namek = {28, 47, 31, 48, 164, 165, 166, 167, 270, 271, 272, 273, 565, 660};
    public static final short[] Ao_Xayda = {2, 49, 5, 50, 168, 169, 170, 171, 238, 239, 240, 241, 559, 654};
    public static final short[] Quan_Xayda = {8, 51, 11, 52, 172, 173, 174, 175, 250, 251, 252, 253, 560, 655};
    public static final short[] Gang_Xayda = {23, 53, 26, 54, 176, 177, 178, 179, 262, 263, 264, 265, 566, 661};
    public static final short[] Giay_Xayda = {29, 55, 32, 56, 180, 181, 182, 183, 274, 275, 276, 277, 567, 662};
    public static final short[] Rada_Gender = {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561, 656};
    public static final short[][][] TrangBiKichHoat = {{Ao_TraiDat, Ao_Namek, Ao_Xayda,}, {Quan_TraiDat, Quan_Namek, Quan_Xayda},
            {Gang_TraiDat, Gang_Namek, Gang_Xayda}, {Giay_TraiDat, Giay_Namek, Giay_Xayda}, {Rada_Gender, Rada_Gender, Rada_Gender, Rada_Gender}};
    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = (byte) Config.gI().getSV_WAIT_LOGIN();
    public static int MAX_PER_IP = Config.gI().getSV_MAXPERIP();
    public static int MAX_PLAYER = Config.gI().getSV_MAXPLAYER();
    public static byte RATE_EXP_SERVER = (byte) Config.gI().getSV_RATE_EXP_SERVER();
    public static boolean LOCAL = Config.gI().isSV_LOCAL();
    public static boolean TEST = Config.gI().isSV_TEST();
    public static boolean DAO_AUTO_UPDATER = Config.gI().isSV_DAO_AUTO_UPDATE();
    public static MapTemplate[] MAP_TEMPLATES;
    public static List<Shop> SHOPS = new ArrayList<>();
    private static GameData instance;
    private final ScheduledExecutorService mapUpdater = Executors.newSingleThreadScheduledExecutor();

//    private GameData() {
//        this.loadDatabase();
//        this.initMap();
//    }

    public static GameData gI() {
        if (instance == null) {
            instance = new GameData();
        }
        return instance;
    }

//          for (int i = 1; i <= 10; i++) {
//            value = properties.get("server.sv" + i);
//            if (value != null) {
//                linkServer += String.valueOf(value) + ":0,";
//            }
//        }
//        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);

    public void load(){

//        String temp = "";
//        for(int i = 0; i < 10; i++){
//            Config.gI().ge
//        }
        this.loadDatabase();
        this.initMap();
    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }

    private void initMap() {
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
        for (MapTemplate mapTemp : MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            map.Map map = new map.Map(
                    mapTemp.id, mapTemp.name, mapTemp.planetId, mapTemp.tileId,
                    mapTemp.bgId, mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                    mapTemp.zones, mapTemp.maxPlayerPerZone, mapTemp.wayPoints
            );
            MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
        }
        new NonInteractiveNPC().initNonInteractiveNPC();
        new Thread(() -> {
            try {
                while (!Maintenance.isRunning) {
                    long st = System.currentTimeMillis();
                    for (map.Map map : MAPS) {
                        for (Zone zone : map.zones) {
                            try {
                                zone.update();
                            } catch (Exception e) {
                                NLogger.logWarning( "Lỗi khi cập nhật zone: " + e.getMessage());
                            }
                        }
                    }
                    long timeDo = System.currentTimeMillis() - st;
                    long sleepTime = 1000 - timeDo;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                }
            } catch (InterruptedException e) {
                NLogger.logWarning( "Thread cập nhật map bị gián đoạn: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                NLogger.logWarning("Lỗi không xác định trong thread cập nhật map: " + e.getMessage());
            }
        }, "Update Maps").start();
    }

    private void loadDatabase() {
        long st = System.currentTimeMillis();
        try (Connection conn = SqlFetcher.getConnection()) {
            new GameDataDAO().loadAll(this, conn);
        } catch (Throwable e) {
            NLogger.logCritical(e, "Fail to load game data.");
            throw new ExceptionInInitializerError();
        }
        NLogger.logInformation("Loaded all game data.(took "+ (System.currentTimeMillis() - st) +"ms)");
    }






    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;

                        }
                    }
                }
            }
        } catch (IOException e) {
            NLogger.logError(e);
        }
        return tileIndexTileType;
    }

    /**
     * @param mapId mapId
     * @return tile map for paint
     */
    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_map_data/" + mapId))) {
                int w = dis.readByte();
                int h = dis.readByte();
                tileMap = new int[h][w];
                for (int[] tm : tileMap) {
                    for (int j = 0; j < tm.length; j++) {
                        tm[j] = dis.readByte();
                    }
                }
            }
        } catch (IOException e) {
        }
        return tileMap;
    }
}
