/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons;

public interface IEntity {
    public static final int chargingTime = 0;
    public static final int weakeningCoeff = 0;
    public static final float damage_min = 0.0f;
    public static final float damage_max = 0.0f;

    public ShotData getShotData();

    public EntityType getType();

    public String toString();
}

