package commands;

import picocli.CommandLine.Command;
import server.DragonBoy;
import java.util.logging.Logger;

@Command(name = "uptime", description = "Show server start time.")
public class UptimeCommand implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(UptimeCommand.class.getName());

    @Override
    public void run() {
        LOGGER.info("Server start time: " + DragonBoy.timeStart);
    }
}