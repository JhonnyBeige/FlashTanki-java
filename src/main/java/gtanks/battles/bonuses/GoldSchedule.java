package gtanks.battles.bonuses;

import gtanks.battles.BattlefieldModel;

public class GoldSchedule implements Runnable {
    private final long time = 28000;
    private final int inc;
    private final Bonus bonus;
    private final BattlefieldModel battlefieldModel;
    private volatile boolean running = true;

    public GoldSchedule(BattlefieldModel bfModel, Bonus bonusToDropp, int incNum) {
        this.battlefieldModel = bfModel;
        this.inc = incNum;
        this.bonus = bonusToDropp;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(this.time);
            if (running) {
                this.battlefieldModel.spawnBonus(bonus, this.inc, 9999);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            e.printStackTrace();
        }
    }

    public void stop() {
        this.running = false;
    }
}
