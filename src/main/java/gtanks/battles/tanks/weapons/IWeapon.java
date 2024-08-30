/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.tanks.weapons;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;

public interface IWeapon {
    public void fire(String var1);

    public void startFire(String var1);

    public void stopFire();

    public void quickFire(String var1);

    public void onTarget(BattlefieldPlayerController[] var1, int var2);

    public IEntity getEntity();
}

