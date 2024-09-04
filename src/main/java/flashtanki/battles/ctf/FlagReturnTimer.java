/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.ctf;

import flashtanki.battles.ctf.flags.FlagServer;

public class FlagReturnTimer
extends Thread {
    public boolean stop = false;
    private final CTFModel ctfModel;
    private final FlagServer flag;

    public FlagReturnTimer(CTFModel ctfModel, FlagServer flag) {
        super.setName("FlagReturnTimer THREAD");
        this.ctfModel = ctfModel;
        this.flag = flag;
    }

    @Override
    public void run() {
        try {
            FlagReturnTimer.sleep(20000L);
            if (!this.stop) {
                this.ctfModel.returnFlag(null, this.flag);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

