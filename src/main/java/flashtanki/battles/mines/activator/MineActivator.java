/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.mines.activator;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.mines.ServerMine;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import java.util.TimerTask;

public class MineActivator
extends TimerTask {
    private static final String PUT_MINE_COMMAND = "put_mine";
    private static final String ACTIVATE_MINE_COMMAND = "activate_mine";
    private final BattlefieldModel bfModel;
    private final ServerMine mine;

    public MineActivator(BattlefieldModel bfModel, ServerMine mine) {
        this.bfModel = bfModel;
        this.mine = mine;
    }

    public void putMine() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, PUT_MINE_COMMAND, JSONUtils.parsePutMineComand(this.mine));
    }

    public void activateMine() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, ACTIVATE_MINE_COMMAND, this.mine.getId());
    }

    @Override
    public void run() {
        this.activateMine();
    }
}

