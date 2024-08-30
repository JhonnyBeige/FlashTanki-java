/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.maps.parser.map.spawn;

public class SpawnPositionType {
    public static final SpawnPositionType BLUE = new SpawnPositionType();
    public static final SpawnPositionType RED = new SpawnPositionType();
    public static final SpawnPositionType NONE = new SpawnPositionType();

    private SpawnPositionType() {
    }

    public static SpawnPositionType getType(String value) {
        if (value.equals("blue")) {
            return BLUE;
        }
        if (value.equals("red")) {
            return RED;
        }
        if (value.equals("dm")) {
            return NONE;
        }
        return NONE;
    }
}

