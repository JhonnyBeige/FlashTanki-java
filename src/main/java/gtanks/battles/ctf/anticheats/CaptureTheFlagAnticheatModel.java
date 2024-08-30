/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.ctf.anticheats;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.ctf.flags.FlagState;
import java.util.HashMap;

public class CaptureTheFlagAnticheatModel {
    private static final long MIN_TIME_DELIVERED = 4000L;
    private HashMap<BattlefieldPlayerController, Data> datas = new HashMap();
    private BattlefieldModel bfModel;

    public CaptureTheFlagAnticheatModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public boolean onTakeFlag(BattlefieldPlayerController taker, FlagServer flag) {
        Data data = this.datas.get(taker);
        if (data == null) {
            data = new Data();
            this.datas.put(taker, data);
        }
        data.lastTimeTakeFlag = System.currentTimeMillis();
        data.prevState = flag.state;
        return false;
    }

    public boolean onDeliveredFlag(BattlefieldPlayerController taker, FlagServer flag) {
        Data data = this.datas.get(taker);
        long time = System.currentTimeMillis() - data.lastTimeTakeFlag;
        if (time <= 4000L && data.prevState == FlagState.BASE) {
            this.bfModel.cheatDetected(taker, this.getClass());
            return true;
        }
        return false;
    }

    class Data {
        long lastTimeTakeFlag;
        long lastTimeDeliveredFlag;
        FlagState prevState;

        Data() {
        }
    }
}

