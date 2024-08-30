/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.effects.activator;

import flashtanki.battles.effects.Effect;

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

