package commands;

import consts.ConstNpc;
import item.Item;
import services.map.ChangeMapService;
import services.map.NpcService;
import network.SessionManager;
import player.Player;
import services.player.InventoryService;
import server.Client;
import server.DragonBoy;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.func.Input;
import utils.SystemMetrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class IngameCommand {

    // ---- Singleton thread-safe
    private IngameCommand() {
        initAdminCommands();
        initParameterizedCommands();
    }
    private static class Holder {
        private static final IngameCommand I = new IngameCommand();
    }
    public static IngameCommand gI() { return Holder.I; }

    // ---- Rate limit nhỏ tránh spam vô ý
    private static final long CMD_COOLDOWN_MS = 300;
    private final Map<Long, Long> lastCmdAt = new ConcurrentHashMap<>();

    private final Map<String, Consumer<Player>> adminCommands = new HashMap<>();
    private final Map<String, BiConsumer<Player, String>> parameterizedCommands = new HashMap<>();

    private void initAdminCommands() {
        adminCommands.put("item", player -> Input.gI().createFormGiveItem(player));
        adminCommands.put("getitem", player -> Input.gI().createFormGetItem(player));
        adminCommands.put("hoiskill", player -> Service.gI().releaseCooldownSkill(player));
        adminCommands.put("d", player -> Service.gI().setPos(player, player.location.x, player.location.y + 10));

        adminCommands.put("menu", player -> NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                "|0|Time start: " + DragonBoy.timeStart
                        + "\nClients: " + Client.gI().getPlayers().size() + " người chơi"
                        + "\nSessions: " + SessionManager.gI().getNumSession()
                        + "\nThreads: " + Thread.activeCount()
                        + "\n" + SystemMetrics.ToString(),
                "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "Đóng"));

     
        adminCommands.put("help", player -> {
            StringBuilder sb = new StringBuilder("Admin commands:\n");
            adminCommands.keySet().stream().sorted().forEach(k -> sb.append("- ").append(k).append('\n'));
            sb.append("Parameterized: m <mapId>, n <taskId>, i <itemId>, toado");
            Service.gI().sendThongBao(player, sb.toString());
        });


        adminCommands.put("toado", p ->
                Service.gI().sendThongBaoOK(p, "x: " + p.location.x + " - y: " + p.location.y));
    }

    private void initParameterizedCommands() {
        // m <mapId>
        parameterizedCommands.put("m ", (player, text) -> {
            Integer mapId = parseIntAfterPrefix(text, "m ");
            if (mapId == null) {
                Service.gI().sendThongBao(player, "Sai định dạng. Dùng: m <mapId>");
                return;
            }
            ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
        });

        // n <taskId>
        parameterizedCommands.put("n ", (player, text) -> {
            Integer idTask = parseIntAfterPrefix(text, "n ");
            if (idTask == null) {
                Service.gI().sendThongBao(player, "Sai định dạng. Dùng: n <taskId>");
                return;
            }
            player.playerTask.taskMain.id = idTask - 1;
            player.playerTask.taskMain.index = 0;
            TaskService.gI().sendNextTaskMain(player);
        });

        // i <itemId>
        parameterizedCommands.put("i ", (player, text) -> {
//            Integer itemId = parseIntAfterPrefix(text, "i ");
            
            
            String[] parts = text.trim().split("\\s+"); 
            
            short itemId = Short.parseShort(parts[1]);
            int quantity = (parts.length >= 3) ? Integer.parseInt(parts[2]) : 1;
            if (quantity <= 0) quantity = 1;

//            if (itemId == null) {
//                Service.gI().sendThongBao(player, "Sai định dạng. Dùng: i <itemId>");
//                return;
//            }
            Item item = ItemService.gI().createNewItem(itemId, quantity);
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) (int) itemId);
            if (ops != null && !ops.isEmpty()) {
                item.itemOptions = ops;
            }
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "GET " + item.template.name + " [" + item.template.id + "] SUCCESS!");
        });
    }

    // ---- API giữ nguyên
    public boolean check(Player player, String text) {

        if (isThrottled(player)) {
            Service.gI().sendThongBao(player, "Thao tác quá nhanh, vui lòng thử lại...");
            return true; // đã xử lý (thông báo) nên trả true
        }

        // Lệnh không tham số
        Consumer<Player> handler = adminCommands.get(text);
        if (handler != null) {
            handler.accept(player);
            touch(player);
            return true;
        }


        for (Map.Entry<String, BiConsumer<Player, String>> e : parameterizedCommands.entrySet()) {
            String prefix = e.getKey();
            if (text.startsWith(prefix)) {
                try {
                    e.getValue().accept(player, text);
                } catch (Exception ex) {
//                    Service.gI().sendThongBao(player, "Command error: " + ex.getMessage());
                }
                touch(player);
                return true;
            }
        }
        return false;
    }

    // ---- helpers
    private static Integer parseIntAfterPrefix(String text, String prefix) {
        if (text == null || prefix == null || !text.startsWith(prefix)) return null;
        String s = text.substring(prefix.length()).trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isThrottled(Player p) {
        long now = System.currentTimeMillis();
        Long last = lastCmdAt.get(p.id); // giả sử player.id là long
        return last != null && (now - last) < CMD_COOLDOWN_MS;
    }
    private void touch(Player p) {
        lastCmdAt.put(p.id, System.currentTimeMillis());
    }
}
