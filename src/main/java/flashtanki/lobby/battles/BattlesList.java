/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.lobby.battles;

import flashtanki.utils.StringUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.services.LobbysServices;
import flashtanki.users.locations.UserLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattlesList {
    private final ArrayList<BattleInfo> battles = new ArrayList();
    private static int countBattles = 0;
    private final LobbysServices lobbysServices = LobbysServices.getInstance();
    private static  BattlesList instance ;
    public static BattlesList getInstance() {
        if (instance == null) {
            instance = new BattlesList();
        }
        return instance;
    }
    private BattlesList() {
    }

    public boolean tryCreateBatle(BattleInfo battleInfo) {
        battleInfo.battleId = generateId(battleInfo.name);
        if (getBattleInfoById(battleInfo.battleId) != null) {
            return false;
        }
        battles.add(battleInfo);
        ++countBattles;
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "create_battle", JSONUtils.parseBattleInfo(battleInfo));
        battleInfo.model = new BattlefieldModel(battleInfo);
        return true;
    }

    public void removeBattle(BattleInfo battle) {
        if (battle == null) {
            return;
        }
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("remove_battle", ";", battle.battleId));
        if (battle.model != null && battle.model.players != null) {
            for (BattlefieldPlayerController player : battle.model.players.values()) {
                player.parentLobby.kick();
            }
        }
        battle.model.destroy();
        battles.remove(battle);
    }

    public List<BattleInfo> getList() {
        return battles;
    }

    private String generateId(String gameName) {
        return new Random().nextInt(50000) + "@" + gameName + "@" + "#" + countBattles;
    }

    public BattleInfo getBattleInfoById(String id) {
        for (BattleInfo battle : battles) {
            if (!battle.battleId.equals(id)) continue;
            return battle;
        }
        return null;
    }
}

