/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.effects.impl;

import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.math.Vector3;

public class HealthEffect extends Effect {
    private int resource;
    private int accumulatedResource;


    private void healTank(int hp) {
        this.player.battle.tanksKillModel.healPlayer(null, this.player, hp);
    }

    @Override
    public void deactivateAction() {
    }

    @Override
    public void activateAction(boolean fromInventory, Vector3 tankPos) {
        int multiplier = fromInventory ? 2 : 1;
        this.resource = (int) (player.tank.getHullInfo().hp * multiplier);
        new Thread(() -> {
            try {
                while (!this.deactivated) {
                    if (this.accumulatedResource + 250 > this.resource) {
                        this.healTank(this.resource - this.accumulatedResource);
                        break;
                    }
                    this.healTank(250);
                    this.accumulatedResource += 250;
                    if (this.player.tank.healthPoints == Tank.MAX_HEALTH_TANK) {
                        break;
                    }
                    Thread.sleep(500L);
                }
                if (!this.deactivated) {
                    this.deactivate();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.HEALTH;
    }

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public int getDurationTime() {
        return 7000;
    }

    @Override
    public int getIneventoryTimeAction() {
        return 6000;
    }

    @Override
    public int getDropTimeAction() {
        return 6000;
    }
}

