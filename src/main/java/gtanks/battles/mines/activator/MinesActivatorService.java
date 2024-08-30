/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.mines.activator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.mines.ServerMine;
import gtanks.battles.mines.activator.MineActivator;
import gtanks.test.osgi.OSGi;
import gtanks.test.server.configuration.entitys.MineConfiguratorEntity;
import java.util.Timer;
import java.util.TimerTask;

public class MinesActivatorService {
    private static final int ACTIVATION_TIME;
    private static final MinesActivatorService instance;
    private static final Timer TIMER;

    static {
        instance = new MinesActivatorService();
        TIMER = new Timer("MinesActivatorService Timer");
        ACTIVATION_TIME = ((MineConfiguratorEntity)OSGi.getModelByInterface(MineConfiguratorEntity.class)).getActivationTimeMsec();
    }

    private MinesActivatorService() {
    }

    public static MinesActivatorService getInstance() {
        return instance;
    }

    public void activate(BattlefieldModel model, ServerMine mine) {
        MineActivator activator = new MineActivator(model, mine);
        TIMER.schedule((TimerTask)activator, ACTIVATION_TIME);
        activator.putMine();
    }
}

