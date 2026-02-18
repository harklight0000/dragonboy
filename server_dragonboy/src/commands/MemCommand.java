package commands;

import picocli.CommandLine.Command;

import java.util.logging.Logger;

@Command(name = "mem", description = "Show JVM memory usage.")
public class MemCommand implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(MemCommand.class.getName());

    @Override
    public void run() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        long max = rt.maxMemory();
        LOGGER.info(String.format("Memory Usage: %s / %s (Max: %s)",
            utils.Util.humanBytes(used),
            utils.Util.humanBytes(total),
            utils.Util.humanBytes(max)));
    }
}