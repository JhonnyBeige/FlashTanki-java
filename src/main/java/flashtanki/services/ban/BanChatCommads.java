/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.services.ban;

public class BanChatCommads {
    public static final String BAN_FIVE_MINUTES = "banminutes";
    public static final String BAN_ONE_HOUR = "banhour";
    public static final String BAN_ONE_DAY = "banday";
    public static final String BAN_ONE_WEEK = "banweek";
    public static final String BAN_ONE_MONTH = "banmonth";
    public static final String BAN_HALF_YEAR = "banhalfyear";
    public static final String BAN_FOREVER = "banforever";

    public static BanTimeType getTimeType(String cmd) {
        BanTimeType time = null;
        switch (cmd) {
            case "banminutes": {
                time = BanTimeType.FIVE_MINUTES;
                break;
            }
            case "banhour": {
                time = BanTimeType.ONE_HOUR;
                break;
            }
            case "banday": {
                time = BanTimeType.ONE_DAY;
                break;
            }
            case "banweek": {
                time = BanTimeType.ONE_WEEK;
                break;
            }
            case "banmonth": {
                time = BanTimeType.ONE_MONTH;
                break;
            }
            case "banhalfyear": {
                time = BanTimeType.HALF_YEAR;
                break;
            }
            case "banforever": {
                time = BanTimeType.FOREVER;
            }
        }
        return time;
    }
}

