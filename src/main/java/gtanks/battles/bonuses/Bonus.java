/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.bonuses;

import gtanks.battles.bonuses.BonusType;
import gtanks.battles.tanks.math.Vector3;

public class Bonus {
    public Vector3 position;
    public BonusType type;
    public long spawnTime;
    public BonusRegion bonusRegion;
    public String id;
    public int inc;

    public Bonus(Vector3 position, BonusType type, BonusRegion region, String id, int inc) {
        this.position = position;
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
        this.bonusRegion = region;
        this.id = id;
        this.inc = inc;
    }
}

