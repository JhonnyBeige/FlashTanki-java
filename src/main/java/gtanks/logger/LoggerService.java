/*
 * Decompiled with CFR 0.150.
 */
package gtanks.logger;

public class LoggerService {
    private static LoggerService instance;
    public static LoggerService getInstance() {
        if (instance == null) {
            instance = new LoggerService();
        }
        return instance;
    }

    public  void log(LogType type, String msg) {
        System.err.println("[" + type.name() + "] " + msg);
    }


    public  void error(Throwable tw) {
        tw.printStackTrace();
    }


}

