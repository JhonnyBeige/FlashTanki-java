/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.bonuses;

import flashtanki.battles.tanks.math.Vector3;

public class BonusRegion {
    public Vector3 max;
    public Vector3 min;
    public String[] types;

    public BonusRegion(Vector3 max, Vector3 min, String[] types) {
        this.max = max;
        this.min = min;
        this.types = types;
    }
}

