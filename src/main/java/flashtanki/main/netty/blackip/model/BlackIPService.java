/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.main.netty.blackip.model;

import flashtanki.collections.FastHashMap;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.main.netty.blackip.BlackIP;

public class BlackIPService {
    private final DatabaseManager database = DatabaseManagerImpl.instance();
    private FastHashMap<String, Boolean> cache = new FastHashMap();

    private static BlackIPService instance;

    private BlackIPService() {
    }

    public static BlackIPService getInstance() {
        if (instance == null) {
            instance = new BlackIPService();
        }
        return instance;
    }

    public boolean contains(String ip) {
        if (this.cache.containsKey(ip)) {
            return true;
        }

        BlackIP obj = this.database.getBlackIPbyAddress(ip);
        boolean result = obj != null;
        if (result) {
            this.cache.put(ip, true);
        }
        return result;
    }

    public void block(String ip) {
        BlackIP blackIP = new BlackIP();
        blackIP.setIp(ip);
        this.database.register(blackIP);
        this.cache.put(ip, true);
    }

    public void unblock(String ip) {
        BlackIP blackIP = new BlackIP();
        blackIP.setIp(ip);
        this.database.unregister(blackIP);
        this.cache.remove(ip);
    }
}

