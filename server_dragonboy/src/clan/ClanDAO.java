package clan;

import clan.Clan;
import clan.ClanMember;
import database.SqlFetcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import logger.MyLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class ClanDAO {


    // -------------------- SQL --------------------
    private static final String INSERT_SQL =
            "INSERT INTO clan (id, name, name_2, slogan, img_id, power_point, max_member, clan_point, level, members, tops) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE clan SET slogan = ?, img_id = ?, power_point = ?, max_member = ?, clan_point = ?, " +
                    "level = ?, members = ?, name_2 = ?, tops = ? WHERE id = ? LIMIT 1";

    private static final String DELETE_SQL =
            "DELETE FROM clan WHERE id = ?";

    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, name_2, slogan, img_id, power_point, max_member, clan_point, level, members, tops " +
                    "FROM clan WHERE id = ? LIMIT 1";


    /** Tạo mới 1 clan */
    public static int insert(Clan c) {
        String membersJson = membersToJson(c);
        String topsJson    = "[]"; // luôn rỗng như code gốc

        try (Connection con = SqlFetcher.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {

            ps.setInt(1,  c.id);
            ps.setString(2, c.name);
            ps.setString(3, c.name2);
            ps.setString(4, c.slogan);
            ps.setInt(5,   c.imgId);
            ps.setLong(6,  c.powerPoint);
            ps.setByte(7,  c.maxMember);
            ps.setInt(8,   c.capsuleClan);
            ps.setInt(9,   c.level);
            ps.setString(10, membersJson);
            ps.setString(11, topsJson);

            return ps.executeUpdate();
        } catch (Exception e) {
            MyLogger.logError(e, "ClanDAO.insert error");
            return 0;
        }
    }

    /** Cập nhật clan */
    public static int update(Clan c) {
        String membersJson = membersToJson(c);
        String topsJson    = "[]"; // đúng logic cũ

        try (Connection con = SqlFetcher.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {

            ps.setString(1,  c.slogan);
            ps.setInt(2,     c.imgId);
            ps.setLong(3,    c.powerPoint);
            ps.setByte(4,    c.maxMember);
            ps.setInt(5,     c.capsuleClan);
            ps.setInt(6,     c.level);
            ps.setString(7,  membersJson);
            ps.setString(8,  c.name2);
            ps.setString(9,  topsJson);
            ps.setInt(10,    c.id);

            return ps.executeUpdate();
        } catch (Exception e) {
            MyLogger.logError(e, "ClanDAO.update error");
            return 0;
        }
    }

    /** Xoá clan theo id */
    public static int delete(int id) {
        try (Connection con = SqlFetcher.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            MyLogger.logError(e, "ClanDAO.delete error");
            return 0;
        }
    }

    /** Tìm clan theo id (mới chỉ map field cơ bản) */
    public static Clan findById(int id) {
        try (Connection con = SqlFetcher.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Clan c = new Clan();
                c.id          = rs.getInt("id");
                c.name        = rs.getString("name");
                c.name2       = rs.getString("name_2");
                c.slogan      = rs.getString("slogan");
                c.imgId       = rs.getInt("img_id");
                c.powerPoint  = rs.getLong("power_point");
                c.maxMember   = rs.getByte("max_member");
                c.capsuleClan = rs.getInt("clan_point");
                c.level       = rs.getInt("level");

                // String membersJson = rs.getString("members"); // TODO: parse nếu cần
                return c;
            }
        } catch (Exception e) {
            MyLogger.logError(e, "ClanDAO.findById error");
            return null;
        }
    }

    // -------------------- Helpers --------------------

    /**
     * Chuyển list ClanMember -> JSON string đúng logic cũ:
     * mảng chứa CHUỖI JSON, không phải object JSON.
     */
    private static String membersToJson(Clan c) {
        JSONArray arr = new JSONArray();
        for (ClanMember cm : c.getMembers()) {
            if (cm == null) continue;
            JSONObject o = new JSONObject();
            o.put("id", cm.id);
            o.put("name", cm.name);
            o.put("head", cm.head);
            o.put("body", cm.body);
            o.put("leg", cm.leg);
            o.put("role", cm.role);
            o.put("donate", cm.donate);
            o.put("receive_donate", cm.receiveDonate);
            o.put("member_point", cm.memberPoint);
            o.put("clan_point", cm.clanPoint);
            o.put("join_time", cm.joinTime);
            o.put("ask_pea_time", cm.timeAskPea);
            o.put("power", cm.powerPoint);

            // LOGIC CŨ: add chuỗi JSON thay vì object
            arr.add(o.toJSONString());
        }
        return arr.toJSONString();
    }
}
