/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons;

import flashtanki.battles.BattlefieldPlayerController;

public interface IWeapon {
    public void fire(String var1);

    public void startFire(String var1);

    public void stopFire();

    public void quickFire(String var1);

    public void onTarget(BattlefieldPlayerController[] var1, int var2);

    public IEntity getEntity();
}

