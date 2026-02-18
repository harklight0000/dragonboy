package logger;

import org.jline.reader.LineReader;

import commands.Shell;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class NLogger {

  
  
    private static class CrititalLevel extends Level {
        protected CrititalLevel() { super("CRITICAL", Level.SEVERE.intValue() + 1); }
    }

  
    public static final Level CRITICAL = new CrititalLevel();

    private static final java.util.logging.Logger JUL_LOGGER =
            java.util.logging.Logger.getLogger("NROServer");

  
    private static final ExecutorService FILE_LOG_EXECUTOR = Executors.newSingleThreadExecutor();

    static {
       
        JUL_LOGGER.setUseParentHandlers(false);

        Handler jlineHandler = new commands.Shell.JLineLogHandler();
        jlineHandler.setLevel(Level.ALL);
        jlineHandler.setFormatter(new CustomFormatter());
        JUL_LOGGER.addHandler(jlineHandler);

        JUL_LOGGER.setLevel(Level.ALL);

        try { LogManager.getLogManager(); } catch (Exception ignored) {}
    }

   
    public static void logDebug(String text) { JUL_LOGGER.log(Level.FINE, text); }
    public static void logInformation(String text) { JUL_LOGGER.log(Level.INFO, text); }
    public static void logWarning(String text) { JUL_LOGGER.log(Level.WARNING, text); }

    public static void logError(Throwable ex, String... msg) {
        String message = (msg != null && msg.length > 0) ? String.join(" ", msg) :
                (ex != null ? ex.getMessage() : "Error");
      
        JUL_LOGGER.log(Level.SEVERE, message, ex);
    }

    public static void logCritical(Throwable ex, String... msg) {
        String message = (msg != null && msg.length > 0) ? String.join(" ", msg) :
                (ex != null ? ex.getMessage() : "Critical");
        JUL_LOGGER.log(CRITICAL, message, ex);
    }

 
    public static void raw(String message) {
        LineReader reader = Shell.READER_REF.get();
        if (reader != null) {
            reader.printAbove(message);
        } else {
            System.out.println(message);
        }
    }

 
    public static void fileLog(String playerName, String message) {
        FILE_LOG_EXECUTOR.submit(() -> {
            try {
                String now = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String logEntry = "[" + now + "] " + message;
                writeFile("log/" + playerName + "_log.txt", logEntry);
            } catch (IOException e) {
                logWarning("Failed to write log file: " + e.getMessage());
            }
        });
    }

    private static void writeFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(content);
        }
    }
}
