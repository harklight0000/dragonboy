package commands;

import player.Player;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import server.Client; // Giả sử class Client tồn tại
import java.util.logging.Logger;

@Command(name = "kick", description = "Disconnect a player from the server.")
public class KickCommand implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(KickCommand.class.getName());

    @Parameters(index = "0", description = "The name of the player to kick.")
    private String playerName;

    @Parameters(index = "1..*", description = "Reason for kicking (optional).", arity = "0..*")
    private String[] reasonParts;

    @Override
    public void run() {
        String reason = (reasonParts != null && reasonParts.length > 0)
                      ? String.join(" ", reasonParts)
                      : "Kicked by admin.";

        Player pl = Client.gI().getPlayer(playerName);
        if (pl == null) {
            LOGGER.warning("Player not found or not online: " + playerName);
            return;
        }

        try {
            Client.gI().kickSession(pl.getSession());
            Client.gI().getPlayers().remove(pl);
            LOGGER.info("Successfully kicked player: " + pl.name + " (Reason: " + reason + ")");
        } catch (Exception e) {
            LOGGER.severe("Failed to kick " + playerName + ": " + e.getMessage());
        }
    }
}