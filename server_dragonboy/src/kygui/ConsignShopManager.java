package kygui;


import java.util.ArrayList;
import java.util.List;

public class ConsignShopManager {

    private static ConsignShopManager instance;
    public long lastTimeUpdate;
    public String[] tabName = {"Áo<>Quần", "Găng<>Tay", "Phụ<>Kiện", "Linh<>tinh", ""};
    public List<ConsignItem> listItem = new ArrayList<>();

    public static ConsignShopManager gI() {
        if (instance == null) {
            instance = new ConsignShopManager();
        }
        return instance;
    }

    public void save() {
        ConsignShopDAO.update(this.listItem);
        this.lastTimeUpdate = System.currentTimeMillis();
    }
}
