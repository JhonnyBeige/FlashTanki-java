/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.services.ban.block;

import java.lang.reflect.Field;

public class BlockGameReason {
    public static final BlockGameReason DEFAULT = new BlockGameReason(0,
            "The account was blocked for violating the rules of the game\nIf you are sure that we made a mistake, write to admin@primetanki.com");
    public static final BlockGameReason UNACCEPTABLE_NICK = new BlockGameReason(1,
            "The account was blocked for using an invalid game name\nIf you are sure that we made a mistake, write to admin@primetanki.com");
    public static final BlockGameReason USING_CHEATS = new BlockGameReason(2,
            "The account was blocked for using cheats\nIf you are sure that we made a mistake, write to admin@primetanki.com");
    public static final BlockGameReason TRANSFER_OF_ACCOUNT = new BlockGameReason(3,
            "The account was blocked for attempting to transfer the account to a third party\nRequests for account restoration will not be accepted.");
    public static final BlockGameReason PUMPING = new BlockGameReason(4,
            "Leveling up (gaining points due to the inaction of another player)\nIf you are sure that we made a mistake, write to admin@primetanki.com");
    public static final BlockGameReason USING_BAGS = new BlockGameReason(5,
            "Malicious use of a software error %nIf you are sure that we made a mistake, write to admin@primetanki.com");
    public static final BlockGameReason SABOTAGE = new BlockGameReason(6,
            "Sabotage (interfering with another team through inactive “fake” players)%nIf you are sure that we made a mistake, write to admin@primetanki.com");
    private final String reason;
    private final int reasonId;

    private BlockGameReason(int reasonId, String reason) {
        this.reason = reason;
        this.reasonId = reasonId;
    }

    public String getReason() {
        return this.reason;
    }

    public int getReasonId() {
        return this.reasonId;
    }

    public static BlockGameReason getReasonById(int i) {
        Class<BlockGameReason> clazz = BlockGameReason.class;
        BlockGameReason reason = DEFAULT;
        try {
            for (Field field : clazz.getFields()) {
                BlockGameReason temp = (BlockGameReason) field.get(null);
                if (temp.reasonId != i)
                    continue;
                reason = temp;
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return reason;
    }
}
