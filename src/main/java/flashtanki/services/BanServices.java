/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.services;

import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.services.ban.BanTimeType;
import flashtanki.services.ban.BanType;
import flashtanki.users.User;
import flashtanki.users.karma.Karma;
import java.util.Calendar;
import java.util.Date;

public class BanServices {
    private static final BanServices instance = new BanServices();
    private final DatabaseManager database = DatabaseManagerImpl.instance();

    private BanServices() {
    }

    public static BanServices getInstance() {
        return instance;
    }

    public void ban(BanType type, BanTimeType time, User user, User banner, String reason) {
        Calendar calendar = Calendar.getInstance();
        Date currDate = new Date();
        calendar.setTime(currDate);
        calendar.add(time.getField(), time.getAmount());
        Date banTo = calendar.getTime();
        if (type == BanType.CHAT) {
            this.banChat(banTo, user, banner, reason);
        } else {
            this.banGame(banTo, user, banner, reason);
        }
    }

    private void banChat(Date date, User user, User banner, String reason) {
        Karma karma = this.database.getKarmaByUser(user);
        karma.setChatBanned(true);
        karma.setChatBannedBefore(date);
        karma.setWhoBannedChatId(banner.getNickname());
        karma.setReasonChatBan(reason);
        this.database.update(karma);
    }

    private void banGame(Date date, User user, User banner, String reason) {
        Karma karma = this.database.getKarmaByUser(user);
        karma.setGameBlocked(true);
        karma.setGameBlockedBefore(date);
        karma.setWhoBannedGameId(banner.getNickname());
        karma.setReasonGameBan(reason);
        this.database.update(karma);
    }

    public void unbanChat(User user) {
        Karma karma = this.database.getKarmaByUser(user);
        karma.clearChatKarma();
        this.database.update(karma);
    }

    public void unblock(User user) {
        Karma karma = this.database.getKarmaByUser(user);
        karma.clearBlockKarma();
        this.database.update(karma);
    }
}

