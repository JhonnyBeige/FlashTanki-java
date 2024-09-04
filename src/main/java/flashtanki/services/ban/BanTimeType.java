/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.services.ban;

import flashtanki.utils.StringUtils;

public class BanTimeType {
    public static final BanTimeType FIVE_MINUTES = new BanTimeType("for 5 minutes.", 12, 5);
    public static final BanTimeType ONE_HOUR = new BanTimeType("for one hour.", 10, 1);
    public static final BanTimeType ONE_DAY = new BanTimeType("for one day.", 5, 1);
    public static final BanTimeType ONE_WEEK = new BanTimeType("for one week.",
            4, 1);
    public static final BanTimeType ONE_MONTH = new BanTimeType("for one months.", 2, 1);
    public static final BanTimeType HALF_YEAR = new BanTimeType(
            "for half an year.", 2, 6);
    public static final BanTimeType FOREVER = new BanTimeType("forever.", 1,
            2);
    private final String nameType;
    private final int field;
    private final int amount;

    private BanTimeType(String nameType, int field, int amount) {
        this.nameType = nameType;
        this.field = field;
        this.amount = amount;
    }

    public String getNameType() {
        return this.nameType;
    }

    public int getField() {
        return this.field;
    }

    public int getAmount() {
        return this.amount;
    }

    public String toString() {
        return StringUtils.concatStrings("BanTimeType [", this.nameType, "]");
    }

    public boolean equals(Object obj) {
        BanTimeType _obj;
        try {
            _obj = (BanTimeType) obj;
        } catch (Exception ex) {
            return false;
        }
        return this.getNameType().equals(_obj.getNameType());
    }
}
