/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons.smoky;

import flashtanki.battles.tanks.weapons.EntityType;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.ShotData;

public class SmokyEntity
implements IEntity {
    public float damage_min;
    public float damage_max;
    private ShotData shotData;
    public final EntityType type = EntityType.SMOKY;

    public SmokyEntity(ShotData shotData, float damage_min, float damage_max) {
        this.damage_min = damage_min;
        this.damage_max = damage_max;
        this.shotData = shotData;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public EntityType getType() {
        return this.type;
    }
}

