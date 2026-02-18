package logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        String levelName;

        if (record.getLevel() == NLogger.CRITICAL) {
            levelName = "CRITICAL";
        } else if (record.getLevel() == Level.SEVERE) {
            levelName = "ERROR";
        } else if (record.getLevel() == Level.WARNING) {
            levelName = "WARN";
        } else if (record.getLevel() == Level.INFO) {
            levelName = "INFO";
        } else if (record.getLevel() == Level.FINE || record.getLevel() == Level.FINER || record.getLevel() == Level.FINEST) {
            levelName = "DEBUG";
        } else {
            levelName = record.getLevel().getName();
        }

        sb.append("[")
          .append(dateFormat.format(new Date(record.getMillis())))
          .append(" ")
          .append(levelName)
          .append("]: ")
          .append(formatMessage(record));

        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(sw));
            sb.append("\n").append(sw);
        }
        sb.append("\n");
        return sb.toString();
    }
}
