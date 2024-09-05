package flashtanki.logger;

import flashtanki.logger.Type;

public class Log {
    public Type type;
    public String msg;

    public Log(Type type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public String toString() {
        return "[" + String.valueOf((Object)this.type) + "]: " + this.msg;
    }
}
