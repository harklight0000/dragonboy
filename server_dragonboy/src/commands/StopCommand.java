package commands;

import picocli.CommandLine.Command;
import server.DragonBoy;
import java.util.logging.Logger;

@Command(name = "stop", description = "Safely shutdown the server.")
public class StopCommand implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(StopCommand.class.getName());

    @Override
    public void run() {
        LOGGER.info("Shutdown command received. Stopping server...");
        Shell.running.set(false); // Ra lệnh cho vòng lặp chính dừng lại
        DragonBoy.gI().close();
    }
}