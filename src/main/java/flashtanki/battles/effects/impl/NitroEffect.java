/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.effects.impl;

import flashtanki.battles.effects.Effect;
import flashtanki.battles.effects.EffectType;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;

public class NitroEffect extends Effect {
    private static final String CHANGE_TANK_SPEC_COMAND = "change_spec_tank";

    @Override
    public void deactivateAction() {
        this.player.tank.speed = this.player.tank.getHullInfo().speed;
        this.player.battle.sendToAllPlayers(Type.BATTLE, CHANGE_TANK_SPEC_COMAND, this.player.tank.id, JSONUtils.parseTankSpec(this.player.tank, true));

    }

    @Override
    public void activateAction(boolean fromInventory, Vector3 tankPos) {
        player.tank.speed = this.addPercent(player.tank.speed, 30);
        player.battle.sendToAllPlayers(Type.BATTLE, CHANGE_TANK_SPEC_COMAND, player.tank.id, JSONUtils.parseTankSpec(player.tank, true));

    }


    @Override
    public EffectType getEffectType() {
        return EffectType.NITRO;
    }

    @Override
    public int getID() {
        return 4;
    }

    private float addPercent(float value, int percent) {
        return value / 100.0f * (float) percent + value;
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

