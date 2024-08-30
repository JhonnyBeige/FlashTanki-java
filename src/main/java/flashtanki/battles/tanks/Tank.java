/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks;

import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.effects.Effect;
import flashtanki.battles.effects.EffectType;
import flashtanki.battles.tanks.data.DamageTankData;
import flashtanki.battles.tanks.hulls.Hull;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.battles.tanks.module.Module;
import flashtanki.battles.tanks.weapons.IWeapon;
import flashtanki.battles.tanks.weapons.flamethrower.effects.FlamethrowerEffectModel;
import flashtanki.battles.tanks.weapons.frezee.effects.FrezeeEffectModel;

import java.util.*;

public class Tank {
    public static final int MAX_HEALTH_TANK = 10000;
    public Vector3 position;
    public Vector3 orientation;
    public Vector3 linVel;
    public Vector3 angVel;
    public double turretDir;
    public int controllBits;
    public FlamethrowerEffectModel flameEffect;
    private IWeapon weapon;
    private Hull hull;
    private Module module;
    private float microUpgradesHealth;
    public String hullId;
    public String turretId;
    public String hullSkin;
    public String turretSkin;
    public String shotEffect;
    public String paintId;
    public String id;
    public float speed;
    public float turnSpeed;
    public float turretRotationSpeed;
    public int healthPoints = MAX_HEALTH_TANK;
    public float maxHp = 0;

    public String state = TankState.newcome;
    public FrezeeEffectModel frezeeEffect;
    public ArrayList<Effect> activeEffects;
    public final Map<EffectType, Long> lockEffects = new HashMap<>();
    public LinkedHashMap<BattlefieldPlayerController, DamageTankData> lastDamagers;

    public Tank(Vector3 position) {
        this.position = position;
        this.activeEffects = new ArrayList();
        this.lastDamagers = new LinkedHashMap();
    }

    public IWeapon getWeaponInfo() {
        return this.weapon;
    }

    public Hull getHullInfo() {
        return this.hull;
    }

    public void setWeapon(IWeapon weapon) {
        this.weapon = weapon;
        this.turretRotationSpeed = weapon.getEntity().getShotData().turretRotationSpeed;
    }

    public void setHull(Hull hull) {
        this.hull = hull;
        this.speed = hull.speed;
        this.turnSpeed = hull.turnSpeed;
    }

    public Module getModule() {
        return this.module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public void setMicroUpgradesHealth(float value) {
        this.microUpgradesHealth = value;
        this.maxHp += value;
    }

    public boolean isUsedEffect(EffectType type) {
        for (Effect effect : this.activeEffects) {
            if (effect.getEffectType() != type)
                continue;
            return true;
        }
        return false;
    }
}
