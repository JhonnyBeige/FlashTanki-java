/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.effects.impl;

import flashtanki.battles.effects.Effect;
import flashtanki.battles.effects.EffectType;
import flashtanki.battles.tanks.math.Vector3;

public class DamageEffect extends Effect {


    @Override
    public void deactivateAction() {

    }

    @Override
    public void activateAction(boolean fromInventory, Vector3 tankPos) {

    }

    @Override
    public EffectType getEffectType() {
        return EffectType.DAMAGE;
    }

    @Override
    public int getID() {
        return 3;
    }

    @Override
    public int getDurationTime() {
        return 60000;
    }

    @Override
    public int getIneventoryTimeAction() {
        return 60000;
    }

    @Override
    public int getDropTimeAction() {
        return 60000;
    }
}

