/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.spectator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.spectator.SpectatorController;
import gtanks.battles.spectator.chat.SpectatorChatModel;
import gtanks.commands.Type;
import gtanks.system.BattlesGC;
import gtanks.system.SystemBattlesHandler;

import java.util.HashMap;

public class SpectatorModel {
    private HashMap<String, SpectatorController> spectators;
    private BattlefieldModel bfModel;
    private SpectatorChatModel chatModel;
    private final BattlesGC battlesGC = BattlesGC.getInstance();

    public SpectatorModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
        this.spectators = new HashMap();
        this.chatModel = new SpectatorChatModel(this);
    }

    public void addSpectator(SpectatorController spec) {
        this.spectators.put(spec.getId(), spec);
        battlesGC.cancelRemoving(this.bfModel);
    }

    public void removeSpectator(SpectatorController spec) {
        this.spectators.remove(spec.getId());
        if (this.bfModel == null) {
            return;
        }
        if (this.bfModel.players == null) {
            return;
        }
        if(this.bfModel.battleInfo.battleId == SystemBattlesHandler.newbieBattleToEnter.battleId ||
        this.bfModel.battleInfo.battleId == SystemBattlesHandler.middleBattle.battleId ||
        this.bfModel.battleInfo.battleId == SystemBattlesHandler.forAllBattle.battleId){
            return;
        }
        if (this.bfModel.players.size() == 0 && this.spectators.size() == 0) {
            battlesGC.addBattleForRemove(this.bfModel);
        }
    }

    public SpectatorChatModel getChatModel() {
        return this.chatModel;
    }

    public BattlefieldModel getBattleModel() {
        return this.bfModel;
    }

    public void sendCommandToSpectators(Type type, String ... args) {
        for (SpectatorController sc : this.spectators.values()) {
            sc.sendCommand(type, args);
        }
    }
}

