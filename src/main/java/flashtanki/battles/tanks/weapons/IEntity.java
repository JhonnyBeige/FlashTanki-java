/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons;

public interface IEntity {
    int chargingTime = 0;
    int weakeningCoeff = 0;
    float damage_min = 0.0f;
    float damage_max = 0.0f;

    ShotData getShotData();

    EntityType getType();

    String toString();
}

