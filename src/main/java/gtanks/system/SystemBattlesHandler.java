package gtanks.system;

import gtanks.battles.maps.MapsLoaderService;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;

public class SystemBattlesHandler {
    public static BattleInfo newbieBattleToEnter;
    public static BattleInfo middleBattle;
    public static BattleInfo forAllBattle;
    public static BattlesList battleList = BattlesList.getInstance();
    public static BattlesGC battlesGC = BattlesGC.getInstance();

    public static void systemBattlesInit() {
        SystemBattlesHandler.newbiesMapConfigSetup();
        SystemBattlesHandler.middleMapConfigSetup();
        SystemBattlesHandler.noLimitMapConfigSetup();
    }

    public static void newbiesMapConfigSetup() {
        BattleInfo battle = new BattleInfo();
        battle.unremoveable = true;
        battle.battleType = "DM";
        battle.team = false;
        battle.numKills = 15;
        battle.minRank = 1;
        battle.maxRank = 4;
        battle.isPaid = false;
        battle.isPrivate = false;
        battle.friendlyFire = false;
        battle.withoutBonuses = true;
        battle.name = "For newbies DM";
        battle.map = MapsLoaderService.maps.get("map_sandbox");
        battle.maxPeople = 12;
        battle.autobalance = true;
        battle.equipmentChange = true;
        battle.time = 600;
        battleList.tryCreateBatle(battle);
        battlesGC.cancelRemoving(battle.model);
        newbieBattleToEnter = battleList.getBattleInfoById(battle.battleId);
    }

    public static void middleMapConfigSetup() {
        BattleInfo battle = new BattleInfo();
        battle.unremoveable = true;
        battle.battleType = "CTF";
        battle.team = true;
        battle.minRank = 5;
        battle.maxRank = 27;
        battle.isPaid = true;
        battle.isPrivate = false;
        battle.friendlyFire = false;
        battle.withoutBonuses = false;
        battle.inventory = false;
        battle.name = "Sandbox CTF XP/BP";
        battle.map = MapsLoaderService.maps.get("map_sandbox");
        battle.maxPeople = 4;
        battle.numFlags = 7;
        battle.autobalance = true;
        battle.equipmentChange = false;
        battle.battleFormat = 1;
        battle.time = 999;
        battleList.tryCreateBatle(battle);
        battlesGC.cancelRemoving(battle.model);
        middleBattle = battleList.getBattleInfoById(battle.battleId);
    }

    public static void noLimitMapConfigSetup() {
        BattleInfo battle = new BattleInfo();
        battle.unremoveable = true;
        battle.battleType = "DM";
        battle.team = false;
        battle.numKills = 300;
        battle.minRank = 1;
        battle.maxRank = 30;
        battle.isPaid = false;
        battle.isPrivate = false;
        battle.friendlyFire = false;
        battle.withoutBonuses = false;
        battle.inventory = true;
        battle.name = "Barda DM";
        battle.map = MapsLoaderService.maps.get("map_barda");
        battle.maxPeople = 26;
        battle.autobalance = true;
        battle.time = 1500;
        battle.microUpgrades = true;
        battle.equipmentChange = true;
        battleList.tryCreateBatle(battle);
        battlesGC.cancelRemoving(battle.model);
        forAllBattle = battleList.getBattleInfoById(battle.battleId);
    }
}
