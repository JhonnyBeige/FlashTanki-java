/*
 * Decompiled with CFR 0.150.
 */
package gtanks.users.karma;

import gtanks.users.karma.UserKarmaAction;
import gtanks.users.karma.UserKarmaActionType;

public class UserKarmaActionFactory {
    public static UserKarmaAction getUserKarmaAction(UserKarmaActionType type, String comment) {
        return new UserKarmaAction(type, comment);
    }

    public static UserKarmaAction getUserKarmaAction(UserKarmaActionType type) {
        return new UserKarmaAction(type);
    }
}

