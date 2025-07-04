/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package flashtanki.battles.tanks.weapons.frezee;

import flashtanki.battles.tanks.weapons.EntityType;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.ShotData;

public class FrezeeEntity
implements IEntity {
    public float damageAreaConeAngle;
    public float damageAreaRange;
    public int energyCapacity;
    public int energyDischargeSpeed;
    public int energyRechargeSpeed;
    public int weaponTickMsec;
    public float coolingSpeed;
    private ShotData shotData;
    public float damage_min;
    public float damage_max;

    public FrezeeEntity(float damageAreaConeAngle, float damageAreaRange, int energyCapacity, int energyDischargeSpeed, int energyRechargeSpeed, int weaponTickMsec, float coolingSpeed, float damage_min, float damage_max, ShotData shotData) {
        this.damageAreaConeAngle = damageAreaConeAngle;
        this.damageAreaRange = damageAreaRange;
        this.energyCapacity = energyCapacity;
        this.energyDischargeSpeed = energyDischargeSpeed;
        this.energyRechargeSpeed = energyRechargeSpeed;
        this.weaponTickMsec = weaponTickMsec;
        this.coolingSpeed = coolingSpeed;
        this.shotData = shotData;
        this.damage_min = damage_min;
        this.damage_max = damage_max;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public EntityType getType() {
        return EntityType.FREZZE;
    }
}

