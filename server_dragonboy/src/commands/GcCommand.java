package commands;

import picocli.CommandLine.Command;
import java.util.logging.Logger;

@Command(name = "gc", description = "Trigger garbage collection.")
public class GcCommand implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(GcCommand.class.getName());

    @Override
    public void run() {
        long before = utils.Util.getUsedMemory();
        LOGGER.info("Triggering Garbage Collection...");
        System.gc();
        long after = utils.Util.getUsedMemory();
        LOGGER.info(String.format("GC complete. Memory usage: %s -> %s",
            utils.Util.humanBytes(before),
            utils.Util.humanBytes(after)));
    }
}