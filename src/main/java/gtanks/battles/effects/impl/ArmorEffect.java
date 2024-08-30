/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.effects.impl;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.activator.EffectActivatorService;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;

import java.util.Optional;
import java.util.TimerTask;

public class ArmorEffect extends Effect {
    @Override
    public void deactivateAction() {

    }

    @Override
    public void activateAction(boolean fromInventory, Vector3 tankPos) {
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.ARMOR;
    }

    @Override
    public int getID() {
        return 2;
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

