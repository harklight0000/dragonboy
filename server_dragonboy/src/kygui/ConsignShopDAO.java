package kygui;

import database.SqlFetcher;
import kygui.ConsignItem;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;


public class ConsignShopDAO {

    private static final String INSERT_SQL =
            "INSERT INTO `shop_ky_gui`" +
                    " (`id`, `player_id`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `isUpTop`, `isBuy`)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static void update(List<ConsignItem> listItem) {
        if (listItem == null) listItem = List.of();

        try (Connection con = SqlFetcher.getConnection();
             Statement truncate = con.createStatement();
             java.sql.PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {

            con.setAutoCommit(false); // bắt đầu transaction

            truncate.execute("TRUNCATE `shop_ky_gui`");

            for (ConsignItem it : listItem) {
                if (it == null) continue;

                // Chuẩn hoá options JSON
                String optionsJson = (it.options == null)
                        ? "[]"
                        : JSONValue.toJSONString(it.options);

                ps.setLong(1, it.id);                // hoặc setObject tuỳ kiểu
                ps.setLong(2, it.player_sell);
                ps.setInt(3, it.tab);
                ps.setInt(4, it.itemId);
                ps.setLong(5, it.goldSell);
                ps.setLong(6, it.gemSell);
                ps.setInt(7, it.quantity);
                ps.setString(8, optionsJson);
                ps.setByte(9, it.isUpTop);        // DB kiểu BOOLEAN/TINYINT
                ps.setBoolean(10, it.isBuy);         // 0/1 tự map

                ps.addBatch();
            }

            ps.executeBatch();
            con.commit();

        } catch (Exception e) {
            // Nên log đầy đủ để biết dữ liệu nào gây lỗi
            e.printStackTrace();
        }
    }
}

//public class ConsignShopDAO {
//    public static void update(List<ConsignItem> listItem){
//        try (Connection con = DatabaseManager.getConnection()) {
//            Statement s = con.createStatement();
//            s.execute("TRUNCATE shop_ky_gui");
//            for (ConsignItem it : listItem) {
//                if (it != null) {
//                    s.execute(String.format("INSERT INTO `shop_ky_gui`(`id`, `player_id`, `tab`, `item_id`,`gold`, `gem`, `quantity`, `itemOption`, `isUpTop`, `isBuy`) VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",
//                            it.id, it.player_sell, it.tab, it.itemId, it.goldSell, it.gemSell, it.quantity, JSONValue.toJSONString(it.options).equals("null") ? "[]" : JSONValue.toJSONString(it.options), it.isUpTop, it.isBuy ? 1 : 0));
//                }
//            }
//        } catch (Exception e) {
//        }
//    }
//}
