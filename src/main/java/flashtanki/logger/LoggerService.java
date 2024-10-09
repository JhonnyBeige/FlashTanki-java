package flashtanki.logger;

import flashtanki.logger.remote.types.LogType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService {
    private static volatile LoggerService instance;

    public static LoggerService getInstance() {
        if (instance == null) {
            synchronized (LoggerService.class) {
                if (instance == null) {
                    instance = new LoggerService();
                }
            }
        }
        return instance;
    }

    public static void log(LogType info, String msg) {
        log(Type.INFO, msg);
    }

    public static void log(Type type, String msg) {
        LogEntry tempLogEntry = new LogEntry(type, msg);
        System.out.println("[" + getCurrentTimeStamp() + "] " + tempLogEntry);
    }

    public static void debug(String msg) {
        log(Type.INFO, msg);
    }

    public static void debug(Type type, String msg) {
        log(type, msg);
    }

    private static String getCurrentTimeStamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").format(LocalDateTime.now());
    }

}
