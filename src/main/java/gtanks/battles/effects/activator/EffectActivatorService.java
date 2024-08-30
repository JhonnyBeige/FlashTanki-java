/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.effects.activator;

import gtanks.battles.effects.Effect;

import java.util.Timer;

public class EffectActivatorService {
    private static EffectActivatorService instance = new EffectActivatorService();
    private static Timer TIMER = new Timer();

    private EffectActivatorService() {
    }

    public static EffectActivatorService getInstance() {
        return instance;
    }

    public void setDeactivateEffectTask(Effect effect, long delay) {
               TIMER.schedule(effect, delay);
    }
}

