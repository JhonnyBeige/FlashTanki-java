package flashtanki.logger;

public class LogEntry {
    public Type type;
    public String msg;

    public LogEntry(Type type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public String toString() {
        return "[" + String.valueOf((Object)this.type) + "]: " + this.msg;
    }
}
