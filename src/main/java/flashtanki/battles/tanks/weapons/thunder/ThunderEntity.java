/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons.thunder;

import flashtanki.battles.tanks.weapons.EntityType;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.ShotData;
import flashtanki.battles.tanks.weapons.WeaponWeakeningData;

public class ThunderEntity
implements IEntity {
    public float maxSplashDamageRadius;
    public float minSplashDamageRadius;
    public float minSplashDamagePercent;
    public float impactForce;
    private ShotData shotData;
    public WeaponWeakeningData wwd;
    public float damage_min;
    public float damage_max;

    public ThunderEntity(float maxSplashDamageRadius, float minSplashDamageRadius, float minSplashDamagePercent, float impactForce, ShotData shotData, float damage_min, float damage_max, WeaponWeakeningData wwd) {
        this.maxSplashDamageRadius = maxSplashDamageRadius;
        this.minSplashDamageRadius = minSplashDamageRadius;
        this.minSplashDamagePercent = minSplashDamagePercent;
        this.impactForce = impactForce;
        this.shotData = shotData;
        this.damage_min = damage_min;
        this.damage_max = damage_max;
        this.wwd = wwd;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public EntityType getType() {
        return EntityType.THUNDER;
    }
}

