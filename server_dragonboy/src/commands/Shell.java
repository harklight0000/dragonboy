package commands;

import commands.*;
import logger.CustomFormatter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import server.DragonBoy;


@Command(
    name = "server-shell",
    mixinStandardHelpOptions = true,
    description = "Server administration shell.",
    subcommands = {
        StopCommand.class,
        UptimeCommand.class,
        MemCommand.class,
        GcCommand.class,
        SayCommand.class,
        KickCommand.class,
        HealthCommand.class
    }
)
public class Shell implements Runnable {

    public static final AtomicBoolean running = new AtomicBoolean(true);
    
    public static final AtomicReference<LineReader> READER_REF = new AtomicReference<>();

    public static void configureLogger() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        Handler jlineHandler = new JLineLogHandler();
        jlineHandler.setFormatter(new CustomFormatter());
        rootLogger.addHandler(jlineHandler);
    }

    public static class JLineLogHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;

            LineReader reader = READER_REF.get();
            String message = getFormatter().format(record).trim();

            if (reader != null) {
                reader.printAbove(message);
            } else {
                System.out.println(message);
            }
        }

        @Override public void flush() {}
        @Override public void close() throws SecurityException {}
    }

  
    @Override
    public void run() {
        CommandLine cmd = new CommandLine(new Shell());
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .appName("ServerShell")
                    .build();
            
            READER_REF.set(reader);

            logger.MyLogger.logInformation("Type 'help' or '--help' for help.");

            while (running.get() && DragonBoy.isRunning) {
                String line;
                try {
                    line = reader.readLine("> ");
                } catch (UserInterruptException | EndOfFileException e) {
                    logger.MyLogger.logWarning("Shutdown signal received. Stopping server...");
                    cmd.execute("stop");
                    return; 
                }

                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                cmd.execute(line.trim().split("\\s+"));
            }
        } catch (IOException e) {
            System.err.println("ConsoleShell I/O error: " + e.getMessage());
        } finally {
            READER_REF.set(null);
            logger.MyLogger.logInformation("Console shell is shutting down...");
        }
    }
}