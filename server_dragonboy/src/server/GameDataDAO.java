package server;



import clan.Clan;
import clan.ClanMember;
import consts.ConstPlayer;
import shop.ShopDAO;
import player.intrinsic.Intrinsic;
import item.Item.ItemOption;
import item.Template.*;
import kygui.ConsignItem;
import kygui.ConsignShopManager;
import giftcode.GiftCodeManager;
import map.WayPoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import giftcode.GiftCodeSystem;
import player.badges.BagesTemplate;
import radar.OptionCard;
import radar.RadarCard;
import server.GameData;
import services.RadarService;
import player.skill.NClass;
import player.skill.Skill;
import task.*;
import logger.NLogger;

import java.io.*;
import java.sql.*;
import java.util.*;

import static network.SessionService.MAP_MOUNT_NUM;


public final class GameDataDAO {

    public void loadAll(GameData gd, Connection conn) throws Exception {
        loadParts(gd, conn);
        loadBgItems(gd, conn);
        loadArrHead2Frames(gd, conn);
        loadClans(gd, conn);
        loadClanNextId(gd, conn);
        loadNClassAndSkills(gd, conn);
        loadHeadAvatars(gd, conn);
        loadFlagBags(gd, conn);
        loadIntrinsics(gd, conn);
        loadTaskMainAndSub(gd, conn);
        loadSideTaskTemplates(gd, conn);
        loadBadgesTaskTemplates(gd, conn);
        loadClanTaskTemplates(gd, conn);
        loadAchievements(gd, conn);
        loadItemTemplatesBatched(gd, conn);
        loadItemOptionTemplates(gd, conn);
        gd.SHOPS = ShopDAO.getShops(conn);
        loadNotify(gd, conn);
        loadImagesByName(gd, conn);
        postProcessMountMap(gd);
        loadConsignShop(gd, conn);
        loadMobTemplates(gd, conn);
        loadNpcTemplates(gd, conn);
        loadBadgesData(gd, conn);
        loadMapTemplates(gd, conn);
        loadRadar(gd, conn);
        loadGiftCodes(gd, conn);
    }



    private void loadParts(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from part");
             ResultSet rs = ps.executeQuery()) {
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                JSONArray dataArray;
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data").replaceAll("\\\"", ""));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    part.partDetails.add(new PartDetail(
                            Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/update_data/part"))) {
                dos.writeShort(parts.size());
                for (Part part : parts) {
                    dos.writeByte(part.type);
                    for (PartDetail partDetail : part.partDetails) {
                        dos.writeShort(partDetail.iconId);
                        dos.writeByte(partDetail.dx);
                        dos.writeByte(partDetail.dy);
                    }
                }
                dos.flush();
            }
        }
    }

    private void loadBgItems(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from bg_item_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BgItem bgItem = new BgItem();
                bgItem.id = rs.getInt("id");
                bgItem.layer = rs.getByte("layer");
                bgItem.dx = rs.getShort("dx");
                bgItem.dy = rs.getShort("dy");
                bgItem.idImage = rs.getShort("image_id");
                gd.BG_ITEMS.add(bgItem);
            }
        }
    }

    private void loadArrHead2Frames(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from array_head_2_frames");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                JSONArray dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int i = 0; i < dataArray.size(); i++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(i).toString()));
                }
                gd.ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
        }
    }

    private void loadClans(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from clan");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JSONArray dataArray;
                JSONObject dataObject;
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.name2 = rs.getString("name_2");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                if (clan.level < 1) clan.level = 1;
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("members"));
                for (int i = 0; i < dataArray.size(); i++) {
                    dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try { cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power"))); }
                    catch (NumberFormatException ignored) {}
                    clan.addClanMember(cm);
                }
                dataArray.clear();
                gd.CLANS.add(clan);
            }
        }
    }

    private void loadClanNextId(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select id from clan order by id desc limit 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.first()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }
        }
    }

    private void loadNClassAndSkills(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from skill_template order by nclass_id, slot");
             ResultSet rs = ps.executeQuery()) {
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                JSONArray dataArray;
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất"
                            : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    gd.NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{")
                );



                for (int j = 0; j < dataArray.size(); j++) {
                    JSONObject dts = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
            }
        }
    }

    private void loadHeadAvatars(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from head_avatar");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                gd.HEAD_AVATARS.add(headAvatar);
            }
        }
    }

    private void loadFlagBags(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from flag_bag");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                gd.FLAGS_BAGS.add(flagBag);
            }
        }
    }

    private void loadIntrinsics(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from intrinsic");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT -> gd.INTRINSIC_TD.add(intrinsic);
                    case ConstPlayer.NAMEC -> gd.INTRINSIC_NM.add(intrinsic);
                    case ConstPlayer.XAYDA -> gd.INTRINSIC_XD.add(intrinsic);
                    default -> {
                        gd.INTRINSIC_TD.add(intrinsic);
                        gd.INTRINSIC_NM.add(intrinsic);
                        gd.INTRINSIC_XD.add(intrinsic);
                    }
                }
                gd.INTRINSICS.add(intrinsic);
            }
        }
    }

    private void loadTaskMainAndSub(GameData gd, Connection conn) throws Exception {
        String sql = """
            SELECT id, task_main_template.name, detail,
                   task_sub_template.name AS sub_name, max_count, notify, npc_id, map
            FROM task_main_template
            JOIN task_sub_template ON task_main_template.id = task_sub_template.task_main_id
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail");
                    gd.TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = rs.getString("sub_name");
                subTask.maxCount = rs.getShort("max_count");
                subTask.notify = rs.getString("notify");
                subTask.npcId = rs.getByte("npc_id");
                subTask.mapId = rs.getShort("map");
                task.subTasks.add(subTask);
            }
        }
    }

    private void loadSideTaskTemplates(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from side_task_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]); sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]); sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]); sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]); sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]); sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                gd.SIDE_TASKS_TEMPLATE.add(sideTask);
            }
        }
    }

    private void loadBadgesTaskTemplates(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from task_badges_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BadgesTaskTemplate t = new BadgesTaskTemplate();
                t.id = rs.getInt("id");
                t.name = rs.getString("NAME");
                t.count = rs.getInt("maxCount");
                t.idbadgesReward = rs.getInt("idbadgesReward");
                gd.TASKS_BADGES_TEMPLATE.add(t);
            }
        }
    }

    private void loadClanTaskTemplates(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from clan_task_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClanTaskTemplate t = new ClanTaskTemplate();
                t.id = rs.getInt("id");
                t.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                t.count[0][0] = Integer.parseInt(mc1[0]); t.count[0][1] = Integer.parseInt(mc1[1]);
                t.count[1][0] = Integer.parseInt(mc2[0]); t.count[1][1] = Integer.parseInt(mc2[1]);
                t.count[2][0] = Integer.parseInt(mc3[0]); t.count[2][1] = Integer.parseInt(mc3[1]);
                t.count[3][0] = Integer.parseInt(mc4[0]); t.count[3][1] = Integer.parseInt(mc4[1]);
                t.count[4][0] = Integer.parseInt(mc5[0]); t.count[4][1] = Integer.parseInt(mc5[1]);
                gd.CLAN_TASKS_TEMPLATE.add(t);
            }
        }
    }

    private void loadAchievements(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from achievement_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                gd.ACHIEVEMENT_TEMPLATE.add(new AchievementTemplate(
                        rs.getString("info1"), rs.getString("info2"),
                        rs.getInt("money"), rs.getLong("max_count")));
            }
        }
    }

    private void loadItemTemplatesBatched(GameData gd, Connection conn) {
        int batchSize = 750;
        int offset = 0;
        try {
            while (true) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM item_template LIMIT ? OFFSET ?")) {
                    ps.setInt(1, batchSize);
                    ps.setInt(2, offset);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) break;
                        do {
                            ItemTemplate itemTemp = new ItemTemplate();
                            itemTemp.id = rs.getShort("id");
                            itemTemp.type = rs.getByte("type");
                            itemTemp.gender = rs.getByte("gender");
                            itemTemp.name = rs.getString("name");
                            itemTemp.description = rs.getString("description");
                            itemTemp.level = rs.getByte("level");
                            itemTemp.iconID = rs.getShort("icon_id");
                            itemTemp.part = rs.getShort("part");
                            itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                            itemTemp.strRequire = rs.getInt("power_require");
                            itemTemp.gold = rs.getInt("gold");
                            itemTemp.gem = rs.getInt("gem");
                            itemTemp.head = rs.getInt("head");
                            itemTemp.body = rs.getInt("body");
                            itemTemp.leg = rs.getInt("leg");
                            gd.ITEM_TEMPLATES.add(itemTemp);
                        } while (rs.next());
                    }
                }
                offset += batchSize;
            }
        } catch (SQLException e) {
            NLogger.logWarning("Error loading item templates: " + e.getMessage());
        }
    }

    private void loadItemOptionTemplates(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select id, name from item_option_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                gd.ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
        }
    }

    private void loadNotify(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from notify order by id desc");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                gd.NOTIFY.add(rs.getString("name") + "<>" + rs.getString("text"));
            }
        }
    }

    private void loadImagesByName(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select name, n_frame from img_by_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                gd.IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
        }
    }

    private void postProcessMountMap(GameData gd) {
        for (ItemTemplate item : gd.ITEM_TEMPLATES) {
            if (item.type == 23 && gd.getNFrameImageByName("mount_" + item.part + "_0") != 0) {
                MAP_MOUNT_NUM.put(item.id, (short) (item.part + 30000));
            }
        }
    }

    private void loadConsignShop(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM shop_ky_gui");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int i = rs.getInt("id");
                int idPl = rs.getInt("player_id");
                byte tab = rs.getByte("tab");
                short itemId = rs.getShort("item_id");
                int gold = rs.getInt("gold");
                int gem = rs.getInt("gem");
                int quantity = rs.getInt("quantity");
                byte isUp = rs.getByte("isUpTop");
                boolean isBuy = rs.getByte("isBuy") == 1;
                List<ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new ItemOption(idOptions, param));
                }
                ConsignShopManager.gI().listItem.add(new ConsignItem(i, itemId, idPl, tab, gold, gem, quantity, isUp, op, isBuy));
            }
        }
    }

    private void loadMobTemplates(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from mob_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getByte("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                gd.MOB_TEMPLATES.add(mobTemp);
            }
        }
    }

    private void loadNpcTemplates(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from npc_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                gd.NPC_TEMPLATES.add(npcTemp);
            }
        }
    }

    private void loadBadgesData(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from data_badges");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BagesTemplate template = new BagesTemplate();
                template.id = rs.getInt("id");
                template.idEffect = rs.getInt("idEffect");
                template.idItem = rs.getInt("idItem");
                template.NAME = rs.getString("NAME");
                JSONArray option = (JSONArray) JSONValue.parse(rs.getString("Options"));
                if (option != null) {
                    for (int u = 0; u < option.size(); u++) {
                        JSONObject jsonobject = (JSONObject) option.get(u);
                        int optionId = Integer.parseInt(jsonobject.get("id").toString());
                        int param = Integer.parseInt(jsonobject.get("param").toString());
                        template.options.add(new ItemOption(optionId, param));
                    }
                }
                gd.BAGES_TEMPLATES.add(template);
            }
        }
    }

    private void loadMapTemplates(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement psCount = conn.prepareStatement("select count(id) from map_template");
             ResultSet rsCount = psCount.executeQuery()) {
            if (rsCount.first()) {
                int countRow = rsCount.getShort(1);
                gd.MAP_TEMPLATES = new MapTemplate[countRow];
                try (PreparedStatement ps = conn.prepareStatement("select * from map_template");
                     ResultSet rs = ps.executeQuery()) {
                    short i = 0;
                    while (rs.next()) {
                        JSONArray dataArray;

                        MapTemplate mapTemplate = new MapTemplate();
                        int mapId = rs.getInt("id");
                        String mapName = rs.getString("name");
                        mapTemplate.id = mapId;
                        mapTemplate.name = mapName;
                        mapTemplate.type = rs.getByte("type");
                        mapTemplate.planetId = rs.getByte("planet_id");
                        mapTemplate.bgType = rs.getByte("bg_type");
                        mapTemplate.tileId = rs.getByte("tile_id");
                        mapTemplate.bgId = rs.getByte("bg_id");
                        mapTemplate.zones = rs.getByte("zones");
                        mapTemplate.maxPlayerPerZone = rs.getByte("max_player");

                        dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                                .replaceAll("\\[\"\\[", "[[")
                                .replaceAll("\\]\"\\]", "]]")
                                .replaceAll("\",\"", ","));
                        for (int j = 0; j < dataArray.size(); j++) {
                            WayPoint wp = new WayPoint();
                            JSONArray dtwp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                            wp.name = String.valueOf(dtwp.get(0));
                            wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                            wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                            wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                            wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                            wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                            wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                            wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                            wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                            wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                            mapTemplate.wayPoints.add(wp);
                            dtwp.clear();
                        }
                        dataArray.clear();

                        dataArray = (JSONArray) JSONValue.parse(rs.getString("mobs").replaceAll("\\\"", ""));
                        mapTemplate.mobTemp = new byte[dataArray.size()];
                        mapTemplate.mobLevel = new byte[dataArray.size()];
                        mapTemplate.mobHp = new int[dataArray.size()];
                        mapTemplate.mobX = new short[dataArray.size()];
                        mapTemplate.mobY = new short[dataArray.size()];
                        for (int j = 0; j < dataArray.size(); j++) {
                            JSONArray dtm = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                            mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                            mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                            mapTemplate.mobHp[j] = Integer.parseInt(String.valueOf(dtm.get(2)));
                            mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                            mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                            dtm.clear();
                        }
                        dataArray.clear();

                        dataArray = (JSONArray) JSONValue.parse(rs.getString("npcs").replaceAll("\\\"", ""));
                        mapTemplate.npcId = new byte[dataArray.size()];
                        mapTemplate.npcX = new short[dataArray.size()];
                        mapTemplate.npcY = new short[dataArray.size()];
                        for (int j = 0; j < dataArray.size(); j++) {
                            JSONArray dtn = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                            mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                            mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                            mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                            dtn.clear();
                        }
                        dataArray.clear();

                        gd.MAP_TEMPLATES[i++] = mapTemplate;
                    }
                }
            }
        }
    }

    private void loadRadar(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select * from radar");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RadarCard rd = new RadarCard();
                rd.Id = rs.getShort("id");
                rd.IconId = rs.getShort("iconId");
                rd.Rank = rs.getByte("rank");
                rd.Max = rs.getByte("max");
                rd.Type = rs.getByte("type");
                rd.Template = rs.getShort("mob_id");
                rd.Name = rs.getString("name");
                rd.Info = rs.getString("info");

                JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("body"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Head = Short.parseShort(ob.get("head").toString());
                        rd.Body = Short.parseShort(ob.get("body").toString());
                        rd.Leg = Short.parseShort(ob.get("leg").toString());
                        rd.Bag = Short.parseShort(ob.get("bag").toString());
                    }
                }

                rd.Options.clear();
                arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Options.add(new OptionCard(
                                Integer.parseInt(ob.get("id").toString()),
                                Short.parseShort(ob.get("param").toString()),
                                Byte.parseByte(ob.get("activeCard").toString())));
                    }
                }
                rd.Require = rs.getShort("require");
                rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
        }
    }

    private void loadGiftCodes(GameData gd, Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM giftcode");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                GiftCodeSystem giftcode = new GiftCodeSystem();
                giftcode.code = rs.getString("code");
                giftcode.id = rs.getInt("id");
                giftcode.countLeft = rs.getInt("count_left");
                if (giftcode.countLeft == -1) giftcode.countLeft = 999_999_999;
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");

                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);
                        int id = Integer.parseInt(jsonObj.get("id").toString());
                        int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                        JSONArray option = (JSONArray) jsonObj.get("options");
                        ArrayList<ItemOption> optionList = new ArrayList<>();
                        if (option != null) {
                            for (int u = 0; u < option.size(); u++) {
                                JSONObject jsonobject = (JSONObject) option.get(u);
                                int optionId = Integer.parseInt(jsonobject.get("id").toString());
                                int param = Integer.parseInt(jsonobject.get("param").toString());
                                optionList.add(new ItemOption(optionId, param));
                            }
                        }
                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }
                GiftCodeManager.gI().listGiftCode.add(giftcode);
            }
        }
    }

    // ====== HẾT CÁC HÀM loadXxx ======
}

/* ===================== GameData.patch =====================

// Chỉ sửa duy nhất loadDatabase() để gọi DAO. Mọi thứ khác giữ nguyên.

private void loadDatabase() {
    long st = System.currentTimeMillis();
    Logger.infoln("Start loading game data");
    try (Connection conn = DatabaseManager.getConnection()) {
        new GameDataDao().loadAll(this, conn);
    } catch (Throwable e) {
        Logger.fatal(e, "Fail to load game data.");
    }
    Logger.successln("Successfully loaded all game data. (took "+ (System.currentTimeMillis() - st) +"ms)");
}

*/
