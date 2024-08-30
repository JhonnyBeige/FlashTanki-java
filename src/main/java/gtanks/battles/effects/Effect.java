/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.effects;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.activator.EffectActivatorService;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;

import java.util.Optional;
import java.util.TimerTask;

public abstract class Effect extends TimerTask {
    protected boolean deactivated;
    protected EffectActivatorService effectActivatorService = EffectActivatorService.getInstance();
    protected BattlefieldPlayerController player;

    public final void activate(BattlefieldPlayerController player, boolean fromInventory, Vector3 tankPos) {
        this.player = player;

        Optional<Effect> existingEffect = player.tank.activeEffects.stream()
                .filter(effect -> effect.getEffectType() == getEffectType())
                .findFirst();

        existingEffect.ifPresentOrElse(effect -> {
                    player.tank.activeEffects.remove(effect);
                    player.tank.activeEffects.add(this);
                    effect.cancel();
                    long delay = fromInventory ? getIneventoryTimeAction() : getDropTimeAction();
                    this.effectActivatorService.setDeactivateEffectTask(this, delay);
                },
                () -> {
                    player.tank.activeEffects.add(this);
                    long delay = fromInventory ? getIneventoryTimeAction() : getDropTimeAction();
                    this.effectActivatorService.setDeactivateEffectTask(this, delay);
                    activateAction(fromInventory, tankPos);
                });
    }

    public final void deactivate() {
        this.deactivated = true;
        this.player.tank.activeEffects.remove(this);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "disnable_effect", this.player.getUser().getNickname(), String.valueOf(this.getID()));
        this.deactivateAction();
    }

    public abstract void deactivateAction();

    public abstract void activateAction(boolean fromInventory, Vector3 tankPos);

    public abstract EffectType getEffectType();

    public abstract int getID();

    public abstract int getDurationTime();

    public abstract int getIneventoryTimeAction();

    public abstract int getDropTimeAction();

    @Override
    public final void run() {
        if (!this.deactivated) {
            this.deactivate();
        }
    }
}

