/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.statistic;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;

public class PlayersStatisticModel {
    private BattlefieldModel bfModel;

    public PlayersStatisticModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void changeStatistic(BattlefieldPlayerController player) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "update_player_statistic", JSONUtils.parsePlayerStatistic(player));
    }
}

