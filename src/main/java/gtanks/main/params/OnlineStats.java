/*
 * Decompiled with CFR 0.150.
 */
package gtanks.main.params;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import gtanks.services.LobbysServices;
import lombok.Getter;

public class OnlineStats {
    @Getter
    private static int online;
    @Getter
    private static int maxOnline;

    public static List<String> onlinePlayers = new ArrayList<>();


    public static Boolean inOnline(final String username) {
        return OnlineStats.onlinePlayers.contains(username);
    }

    public static void addOnline(String username) {
        ++online;
        maxOnline = Math.max(maxOnline, online);
        onlinePlayers.add(username);
    }

    public static void removeInOnline(String nickname) {
        --online;
        OnlineStats.onlinePlayers.remove(nickname);
    }
}

