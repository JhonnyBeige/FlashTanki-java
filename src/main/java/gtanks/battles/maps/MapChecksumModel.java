/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.maps;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;

@AnticheatModel(name="MapChecksumModel", actionInfo="\u041f\u0440\u043e\u0432\u0435\u0440\u044f\u0435\u0442 \u0447\u0435\u043a-\u0441\u0443\u043c\u0443(md5) \u043a\u0430\u0440\u0442\u044b \u043d\u0430 \u043a\u043b\u0438\u0435\u043d\u0442\u0435")
public class MapChecksumModel {
    private BattlefieldModel bfModel;

    public MapChecksumModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void check(BattlefieldPlayerController player, String hashSum) {
        this.bfModel.battleInfo.map.md5Hash.equals(hashSum);
    }
}

