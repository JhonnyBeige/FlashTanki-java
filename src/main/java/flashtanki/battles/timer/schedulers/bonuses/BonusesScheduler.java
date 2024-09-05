/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.timer.schedulers.bonuses;

import flashtanki.battles.BattlefieldModel;
import flashtanki.commands.Type;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BonusesScheduler {
    private static final Timer TIMER = new Timer("BonusesScheduler timer");
    private static HashMap<String, RemoveBonusTask> tasks = new HashMap();

    public static void runRemoveTask(BattlefieldModel bfModel, String bonusId, long disappearingTime) {
        RemoveBonusTask rbt = new RemoveBonusTask();
        rbt.bfModel = bfModel;
        rbt.bonusId = bonusId;
        tasks.put(bonusId, rbt);
        TIMER.schedule((TimerTask)rbt, disappearingTime * 1000L - 1250L);
    }

    static class RemoveBonusTask
    extends TimerTask {
        public String bonusId;
        public BattlefieldModel bfModel;

        RemoveBonusTask() {
        }

        @Override
        public void run() {
            if (this.bfModel == null) {
                return;
            }
            if (this.bfModel.activeBonuses == null) {
                return;
            }
            this.bfModel.activeBonuses.remove(this.bonusId);
            this.bfModel.sendToAllPlayers(Type.BATTLE, "remove_bonus", this.bonusId);
            tasks.remove(this.bonusId);
        }
    }
}

