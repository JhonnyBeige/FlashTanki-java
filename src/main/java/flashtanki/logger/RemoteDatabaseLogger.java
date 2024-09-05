/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.logger;

import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class RemoteDatabaseLogger {
    private static final DatabaseManager databaseManager = DatabaseManagerImpl.instance();

    public static void error(Exception ex) {
        RemoteDatabaseLogger.log(RemoteDatabaseLogger.buildLogObject(ex, LogType.ERROR));
    }

    public static void error(String message) {
        RemoteDatabaseLogger.log(RemoteDatabaseLogger.buildLogObject(message, LogType.ERROR));
    }

    public static void info(Exception ex) {
        RemoteDatabaseLogger.log(RemoteDatabaseLogger.buildLogObject(ex, LogType.INFO));
    }

    public static void warn(Exception ex) {
        RemoteDatabaseLogger.log(RemoteDatabaseLogger.buildLogObject(ex, LogType.WARNING));
    }

    public static void crit(Exception ex) {
        RemoteDatabaseLogger.log(RemoteDatabaseLogger.buildLogObject(ex, LogType.CRITICAL_ERROR));
    }

    public static String dumpLogs() {
        StringBuilder sb = new StringBuilder();
        List<LogObject> logs = databaseManager.collectLogs();
        ArrayList<DumpData> dump = new ArrayList<DumpData>();
        block0: for (LogObject log : logs) {
            DumpData dd = new DumpData();
            dd.obj = log;
            if (dump.contains(dd)) {
                for (DumpData _dd : dump) {
                    if (!_dd.equals(dd)) continue;
                    ++_dd.count;
                    continue block0;
                }
                continue;
            }
            dump.add(dd);
        }
        for (DumpData _dd : dump) {
            sb.append("[count :").append(_dd.count).append("] ").append(_dd.obj.toString()).append("\n");
        }
        return sb.toString();
    }

    public static List<DumpData> getDump() {
        List<LogObject> logs = databaseManager.collectLogs();
        ArrayList<DumpData> dump = new ArrayList<DumpData>();
        block0: for (LogObject log : logs) {
            DumpData dd = new DumpData();
            dd.obj = log;
            if (dump.contains(dd)) {
                for (DumpData _dd : dump) {
                    if (!_dd.equals(dd)) continue;
                    ++_dd.count;
                    continue block0;
                }
                continue;
            }
            dump.add(dd);
        }
        return dump;
    }

    private static LogObject buildLogObject(Exception ex, LogType type) {
        LogObject log = new LogObject();
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        log.setDate(new Date());
        log.setType(type);
        log.setMessage(exceptionAsString);
        return log;
    }

    private static LogObject buildLogObject(String msg, LogType type) {
        LogObject log = new LogObject();
        log.setDate(new Date());
        log.setType(type);
        log.setMessage(msg);
        return log;
    }

    private static void log(LogObject obj) {
        if (obj != null) {
            databaseManager.register(obj);
        }
    }

    public static class DumpData {
        public LogObject obj;
        public int count = 1;

        public String getHeader() {
            StringTokenizer st = new StringTokenizer(this.obj.getMessage());
            return st.nextToken("\n");
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof DumpData)) {
                return false;
            }
            DumpData _obj = (DumpData)obj;
            return this.obj.getMessage().equals(_obj.obj.getMessage());
        }
    }
}

