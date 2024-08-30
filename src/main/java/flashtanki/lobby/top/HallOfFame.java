/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.lobby.top;

import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.users.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HallOfFame {
    private static final HallOfFame instance = new HallOfFame();
    private static final LoggerService loggerService = LoggerService.getInstance();
    private List<User> top = new ArrayList(100);

    public static HallOfFame getInstance() {
        return instance;
    }

    private HallOfFame() {
    }

    public void addUser(User user) {
        loggerService.log(LogType.INFO,"User " + user.getNickname() + " has been added to top. " + (this.top.add(user) ? "DONE" : "ERROR"));
    }

    public void removeUser(User user) {
        loggerService.log(LogType.INFO,"User " + user.getNickname() + " has been removed of top. " + (this.top.remove(user) ? "DONE" : "ERROR"));
    }

    public void initHallFromCollection(Collection<? extends User> collection) {
        this.top = new ArrayList<User>(collection);
    }

    public List<User> getData() {
        return this.top;
    }
}

