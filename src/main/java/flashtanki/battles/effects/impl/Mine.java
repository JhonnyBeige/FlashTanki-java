/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.effects.impl;

import flashtanki.battles.effects.Effect;
import flashtanki.battles.effects.EffectType;
import flashtanki.battles.tanks.math.Vector3;

public class Mine extends  Effect {
    @Override
    public void deactivateAction() {

    }

    @Override
    public void activateAction(boolean fromInventory, Vector3 tankPos) {
        if (!fromInventory) {
            throw new IllegalArgumentException("Effect 'Mine' was not caused from inventory!");
        }
        player.battle.battleMinesModel.tryPutMine(player, tankPos);

    }

    @Override
    public EffectType getEffectType() {
        return EffectType.MINE;
    }

    @Override
    public int getID() {
        return 5;
    }

    @Override
    public int getDurationTime() {
        return 30000;
    }

    @Override
    public int getIneventoryTimeAction() {
        return 0;
    }

    @Override
    public int getDropTimeAction() {
        return 0;
    }
}

