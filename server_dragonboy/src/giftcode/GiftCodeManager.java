package giftcode;

import database.SqlFetcher;
import services.map.NpcService;
import player.Player;
import services.player.InventoryService;
import services.Service;

import java.util.ArrayList;

public class GiftCodeManager {

    private static GiftCodeManager instance;
    public final ArrayList<GiftCodeSystem> listGiftCode = new ArrayList<>();
    public String name;

    public static GiftCodeManager gI() {
        if (instance == null) {
            instance = new GiftCodeManager();
        }
        return instance;
    }

    public GiftCodeSystem checkUseGiftCode(Player player, String code) {
        for (GiftCodeSystem giftCode : listGiftCode) {
            if (giftCode.code.equals(code)) {
                if (giftCode.countLeft <= 0) {
                    Service.gI().sendThongBaoOK(player, "Giftcode đã hết");
                    return null;
                } else if (giftCode.isUsedGiftCode(player)) {
                    Service.gI().sendThongBaoOK(player, "Bạn đã nhập giftcode này rồi");
                    return null;
                }
                if (InventoryService.gI().getCountEmptyBag(player) < giftCode.detail.size()) {
                    Service.gI().sendThongBaoOK(player, "Cần tối thiểu " + giftCode.detail.size() + " ô hành trang trống");
                    return null;
                }
                giftCode.countLeft -= 1;
                player.giftCode.add(code);
                updateGiftCode(giftCode);
                return giftCode;
            }
        }
        return null;
    }

    public void updateGiftCode(GiftCodeSystem giftcode) {
        try {
            SqlFetcher.executeUpdate("update giftcode set count_left = ? where id = ?", giftcode.countLeft, giftcode.id);
        } catch (Exception e) {
        }
    }

    public void checkInfomationGiftCode(Player p) {
        StringBuilder sb = new StringBuilder();
        for (GiftCodeSystem giftCode : listGiftCode) {
            sb.append("Code: ").append(giftCode.code).append(", Số lượng còn lại: ").append(giftCode.countLeft).append("\b")
                    .append("Ngày tạo: ")
                    .append(giftCode.datecreate).append(", Ngày hết hạn: ").append(giftCode.dateexpired)
                    .append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        NpcService.gI().createTutorial(p, 5073, sb.toString());
    }

}