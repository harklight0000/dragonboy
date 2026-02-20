package logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class MyLogger {
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger("NROServer");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final ExecutorService FILE_LOG_EXECUTOR = Executors.newSingleThreadExecutor();

    static {
        JUL_LOGGER.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "[" + DATE_FORMAT.format(new Date(record.getMillis())) + "] "
                     + "[" + record.getLevel() + "]: "
                     + formatMessage(record) + "\n";
            }
        });
        JUL_LOGGER.addHandler(consoleHandler);
    }

    public static void logInformation(String text) { JUL_LOGGER.info(text); }
    public static void logWarning(String text) { JUL_LOGGER.warning(text); }
    public static void logError(Throwable ex, String text) { JUL_LOGGER.log(Level.SEVERE, text, ex); }

    public static void logError(String text) { JUL_LOGGER.log(Level.SEVERE, text); }
    public static void logError(Throwable ex) { JUL_LOGGER.log(Level.SEVERE, ex.toString(), ex); }

    public static void fileLog(String playerName, String message) {
        FILE_LOG_EXECUTOR.submit(() -> {
            try {
                String log = "[" + DATE_FORMAT.format(new Date()) + "] " + message + "\n";
                writeFile("log/players/" + playerName + ".log", log);
            } catch (Exception ignored) {}
        });
    }

    private static void writeFile(String path, String content) throws IOException {
        File f = new File(path);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(f, true)) {
            fw.write(content);
        }
    }
}