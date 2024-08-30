/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.system;

import flashtanki.battles.BattlefieldModel;
import flashtanki.lobby.battles.BattlesList;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.services.AutoEntryServices;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BattlesGC {
    private static final long TIME_FOR_REMOVING_EMPTY_BATTLE = 20000L;
    private static final HashMap<BattlefieldModel, Timer> battlesForRemove = new HashMap<>();
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.getInstance();
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static final BattlesList battlesList = BattlesList.getInstance();
    private static BattlesGC instance;
    public static BattlesGC getInstance() {
        if (instance == null) {
            instance = new BattlesGC();
        }
        return instance;
    }
    private BattlesGC() {
    }

    public void addBattleForRemove(BattlefieldModel battle) {
        if (battle == null) {
            return;
        }
        Timer timer = new Timer("BattlesGC::Timer for battle: " + battle.battleInfo.battleId);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                removeEmptyBattle(battle);
            }
        }, TIME_FOR_REMOVING_EMPTY_BATTLE);
        battlesForRemove.put(battle, timer);
    }

    public void cancelRemoving(BattlefieldModel model) {
        Timer timer = battlesForRemove.get(model);
        if (timer == null) {
            return;
        }
        timer.cancel();
        battlesForRemove.remove(model);
    }

    private void removeEmptyBattle(BattlefieldModel battle) {
        loggerService.log(LogType.INFO, "[BattlesGarbageCollector]: battle[" + battle.battleInfo + "] has been deleted by inactivity.");
        battlesList.removeBattle(battle.battleInfo);
        autoEntryServices.battleDisposed(battle);
    }


}

