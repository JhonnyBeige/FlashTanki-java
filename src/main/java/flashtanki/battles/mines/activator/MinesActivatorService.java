/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.mines.activator;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.mines.ServerMine;
import flashtanki.configurator.osgi.OSGi;
import flashtanki.configurator.server.configuration.entitys.MineConfiguratorEntity;
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

