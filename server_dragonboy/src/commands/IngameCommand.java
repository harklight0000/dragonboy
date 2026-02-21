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
import player.Pet;
import services.PetService;

public final class IngameCommand {

    private static final long CMD_COOLDOWN_MS = 300;
    private final Map<Long, Long> lastCmdAt = new ConcurrentHashMap<>();

    private final Map<String, Consumer<Player>> adminCommands = new HashMap<>();
    private final Map<String, Consumer<Player>> commonCommands = new HashMap<>();
    private final Map<String, BiConsumer<Player, String>> parameterizedCommands = new HashMap<>();

    private IngameCommand() {
        initCommonCommands();
        initAdminCommands();
        initParameterizedCommands();
    }

    private static class Holder {
        private static final IngameCommand I = new IngameCommand();
    }

    public static IngameCommand gI() {
        return Holder.I;
    }

    private void initCommonCommands() {
        commonCommands.put("di theo", p -> changePetStatus(p, Pet.FOLLOW));
        commonCommands.put("follow", p -> changePetStatus(p, Pet.FOLLOW));
        commonCommands.put("bao ve", p -> changePetStatus(p, Pet.PROTECT));
        commonCommands.put("protect", p -> changePetStatus(p, Pet.PROTECT));
        commonCommands.put("tan cong", p -> changePetStatus(p, Pet.ATTACK));
        commonCommands.put("attack", p -> changePetStatus(p, Pet.ATTACK));
        commonCommands.put("ve nha", p -> changePetStatus(p, Pet.GOHOME));
        commonCommands.put("go home", p -> changePetStatus(p, Pet.GOHOME));
        commonCommands.put("bien hinh", p -> {
            if (p.pet != null) p.pet.transform();
        });
    }

    private void initAdminCommands() {
        adminCommands.put("item", player -> Input.gI().createFormGiveItem(player));
        adminCommands.put("getitem", player -> Input.gI().createFormGetItem(player));
        adminCommands.put("hoiskill", player -> Service.gI().releaseCooldownSkill(player));
        adminCommands.put("d", player -> Service.gI().setPos(player, player.location.x, player.location.y + 10));
        adminCommands.put("toado", p -> Service.gI().sendThongBaoOK(p, "x: " + p.location.x + " - y: " + p.location.y));

        adminCommands.put("menu", player -> NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                "|0|Time start: " + DragonBoy.timeStart
                        + "\nClients: " + Client.gI().getPlayers().size() + " người chơi"
                        + "\nSessions: " + SessionManager.gI().getNumSession()
                        + "\nThreads: " + Thread.activeCount()
                        + "\n" + SystemMetrics.ToString(),
                "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "Đóng"));

        adminCommands.put("help", player -> {
            StringBuilder sb = new StringBuilder("--- Player Commands ---\n");
            commonCommands.keySet().stream().sorted().forEach(k -> sb.append("- ").append(k).append('\n'));
            sb.append("- ten con la <name>\n");
            
            if (player.isAdmin()) {
                sb.append("\n--- Admin Commands ---\n");
                adminCommands.keySet().stream().sorted().forEach(k -> sb.append("- ").append(k).append('\n'));
                sb.append("Parameterized: m <id>, n <id>, i <id> <q>\n");
            }
            Service.gI().sendThongBao(player, sb.toString());
        });
    }

    private void initParameterizedCommands() {
        parameterizedCommands.put("ten con la ", (p, text) -> {
            String newName = text.substring("ten con la ".length()).trim();
            PetService.gI().changeNamePet(p, newName);
        });

        parameterizedCommands.put("m ", (p, text) -> {
            if (!p.isAdmin()) return;
            Integer mapId = parseIntAfterPrefix(text, "m ");
            if (mapId != null) ChangeMapService.gI().changeMapInYard(p, mapId, -1, -1);
        });

        parameterizedCommands.put("n ", (p, text) -> {
            if (!p.isAdmin()) return;
            Integer idTask = parseIntAfterPrefix(text, "n ");
            if (idTask != null) {
                p.playerTask.taskMain.id = idTask - 1;
                p.playerTask.taskMain.index = 0;
                TaskService.gI().sendNextTaskMain(p);
            }
        });

        parameterizedCommands.put("i ", (p, text) -> {
            if (!p.isAdmin()) return;
            String[] parts = text.trim().split("\\s+");
            if (parts.length < 2) return;
            
            short itemId = Short.parseShort(parts[1]);
            int quantity = (parts.length >= 3) ? Integer.parseInt(parts[2]) : 1;
            
            Item item = ItemService.gI().createNewItem(itemId, Math.max(1, quantity));
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itemId);
            if (ops != null) item.itemOptions = ops;
            
            InventoryService.gI().addItemBag(p, item);
            InventoryService.gI().sendItemBags(p);
            Service.gI().sendThongBao(p, "GET " + item.template.name + " SUCCESS!");
        });
    }

    public boolean check(Player player, String text) {
        if (isThrottled(player)) {
            Service.gI().sendThongBao(player, "Thao tác quá nhanh...");
            return true;
        }

        Consumer<Player> common = commonCommands.get(text);
        if (common != null) {
            common.accept(player);
            touch(player);
            return true;
        }

        if (player.isAdmin()) {
            Consumer<Player> admin = adminCommands.get(text);
            if (admin != null) {
                admin.accept(player);
                touch(player);
                return true;
            }
        }

        for (Map.Entry<String, BiConsumer<Player, String>> e : parameterizedCommands.entrySet()) {
            if (text.startsWith(e.getKey())) {
                e.getValue().accept(player, text);
                touch(player);
                return true;
            }
        }

        return false;
    }

    private void changePetStatus(Player p, int status) {
        if (p.pet != null) {
            p.pet.changeStatus((byte) status);
        }
    }

    private static Integer parseIntAfterPrefix(String text, String prefix) {
        try {
            String s = text.substring(prefix.length()).trim();
            return s.isEmpty() ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isThrottled(Player p) {
        long now = System.currentTimeMillis();
        Long last = lastCmdAt.get(p.id);
        return last != null && (now - last) < CMD_COOLDOWN_MS;
    }

    private void touch(Player p) {
        lastCmdAt.put(p.id, System.currentTimeMillis());
    }
}