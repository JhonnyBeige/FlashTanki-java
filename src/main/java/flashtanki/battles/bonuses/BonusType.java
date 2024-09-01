/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.bonuses;

public enum BonusType {
    GOLD{

        public String toString() {
            return "gold";
        }
    }
    ,
    ARMOR{

        public String toString() {
            return "armor";
        }
    }
    ,
    HEALTH{

        public String toString() {
            return "health";
        }
    }
    ,
    DAMAGE{

        public String toString() {
            return "damage";
        }
    }
    ,
    NITRO{

        public String toString() {
            return "nitro";
        }
    };


    private BonusType() {
    }

    /* synthetic */ BonusType(String string, int n, BonusType bonusType) {
        this();
    }
}

