/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.tanks.weapons.frezee.effects;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.weapons.effects.IEffect;
import gtanks.battles.tanks.weapons.frezee.effects.TemperatureCalc;
import gtanks.commands.Type;
import gtanks.json.JSONUtils;

public class FrezeeEffectModel
implements IEffect {
    private static final float MIN_VALUE = 0.5F;
    public float speed;
    public float turnSpeed;
    public float turretRotationSpeed;
    private float power;
    private Tank tank;
    private BattlefieldModel bfModel;
    private FrezeeTimer currFrezeeTimer;

    public FrezeeEffectModel(float power, Tank tank, BattlefieldModel bfModel) {
        this.power = power;
        this.tank = tank;
        this.bfModel = bfModel;
    }

    public void setStartSpecFromTank() {
        this.speed = this.tank.speed;
        this.turnSpeed = this.tank.turnSpeed;
        this.turretRotationSpeed = this.tank.turretRotationSpeed;
    }

    @Override
    public void update() {
        this.tank.speed -= this.power * (this.speed / 100.0F * this.power);
        this.tank.turnSpeed -= this.power * (this.turnSpeed / 100.0F * this.power);
        this.tank.turretRotationSpeed -= this.power * (this.turretRotationSpeed / 100.0F * this.power);
        if (this.tank.speed < MIN_VALUE) {
            this.tank.speed = MIN_VALUE;
        }
        if (this.tank.turnSpeed < MIN_VALUE) {
            this.tank.turnSpeed = MIN_VALUE;
        }
        if (this.tank.turretRotationSpeed < MIN_VALUE) {
            this.tank.turretRotationSpeed = MIN_VALUE;
        }
        if (this.currFrezeeTimer != null) {
            this.currFrezeeTimer.stoped = true;
        }
        this.currFrezeeTimer = new FrezeeTimer();
        this.currFrezeeTimer.start();
        this.sendSpecData();
        this.sendChangeTemperature(TemperatureCalc.getTemperature(this.tank, this.speed, this.turnSpeed, this.turretRotationSpeed));
    }

    private void sendSpecData() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_spec_tank", this.tank.id, JSONUtils.parseTankSpec(this.tank, false));
    }

    private void sendChangeTemperature(double value) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_temperature_tank", this.tank.id, String.valueOf(value));
    }

    class FrezeeTimer
    extends Thread {
        public boolean stoped = false;

        FrezeeTimer() {
        }

        @Override
        public void run() {
            this.setName("FREZEE TIMER THREAD " + FrezeeEffectModel.this.tank);
            try {
                FrezeeTimer.sleep(4000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.stoped) {
                return;
            }
            ((FrezeeEffectModel)FrezeeEffectModel.this).tank.speed = (((FrezeeEffectModel)FrezeeEffectModel.this).tank.speed + FrezeeEffectModel.this.speed) / 2;
            ((FrezeeEffectModel)FrezeeEffectModel.this).tank.turnSpeed = (((FrezeeEffectModel)FrezeeEffectModel.this).tank.turnSpeed + FrezeeEffectModel.this.turnSpeed) / 2;
            ((FrezeeEffectModel)FrezeeEffectModel.this).tank.turretRotationSpeed = (((FrezeeEffectModel)FrezeeEffectModel.this).tank.turretRotationSpeed + FrezeeEffectModel.this.turretRotationSpeed) / 2;
            FrezeeEffectModel.this.sendSpecData();
            
            try {
                FrezeeTimer.sleep(1500L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.stoped) {
                return;
            }
            ((FrezeeEffectModel)FrezeeEffectModel.this).tank.speed = FrezeeEffectModel.this.speed;
            ((FrezeeEffectModel)FrezeeEffectModel.this).tank.turnSpeed = FrezeeEffectModel.this.turnSpeed;
            ((FrezeeEffectModel)FrezeeEffectModel.this).tank.turretRotationSpeed = FrezeeEffectModel.this.turretRotationSpeed;
            FrezeeEffectModel.this.sendSpecData();
            FrezeeEffectModel.this.sendChangeTemperature(0.0);
        }
    }
}

