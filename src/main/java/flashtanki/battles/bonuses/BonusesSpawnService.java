/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.bonuses;

import flashtanki.utils.RandomUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.spectator.SpectatorController;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.json.JSONUtils;
import flashtanki.logger.LogType;

import java.util.ArrayList;
import java.util.Random;

import flashtanki.logger.LoggerService;
import org.json.simple.JSONObject;

import java.util.Map;

public class BonusesSpawnService
        implements Runnable {
    private static final int DISAPPEARING_TIME_DROP = 30;
    private static final int DISAPPEARING_TIME_MONEY = 300;

    private final LoggerService loggerService = LoggerService.getInstance();
    private final Random random = new Random();

    public BattlefieldModel battlefieldModel;
    private int inc = 0;
    private int prevFund = 0;
    private int crystallFund;
    private int goldFund;
    private int nextGoldFund;

    public BonusesSpawnService(BattlefieldModel model) {
        this.battlefieldModel = model;
        this.nextGoldFund = (int) RandomUtils.getRandom(7000.0f, 7300.0f);
    }

    public void spawnRandomDrop() {
        int id = this.random.nextInt(4);
        BonusType bonusType = null;
        switch (id) {
            case 0: {
                bonusType = BonusType.ARMOR;
                break;
            }
            case 1: {
                bonusType = BonusType.HEALTH;
                break;
            }
            case 2: {
                bonusType = BonusType.DAMAGE;
                break;
            }
            case 3: {
                bonusType = BonusType.NITRO;
            }
        }
        int count = this.random.nextInt(4);
        for (int i = 0; i < count; ++i) {
            this.spawnBonus(bonusType);
        }
    }

    public void spawnRandomBonus() {
        boolean wasSpawned = this.random.nextBoolean();
        if (wasSpawned && this.battlefieldModel.players.size() > 0) {
            int id = this.random.nextInt(5);
            BonusType bonusType = null;
            switch (id) {
                case 0: {
                    bonusType = BonusType.NITRO;
                    break;
                }
                case 1: {
                    bonusType = BonusType.ARMOR;
                    break;
                }
                case 2: {
                    bonusType = BonusType.HEALTH;
                    break;
                }
                case 3: {
                    bonusType = BonusType.DAMAGE;
                    break;
                }
                case 4: {
                    bonusType = BonusType.NITRO;
                }
            }
            int count = this.random.nextInt(4);
            for (int i = 0; i < count; ++i) {
                this.spawnBonus(bonusType);
            }
        }
    }

    private ArrayList<BonusRegion> getRegionType(BonusType type) {
        if (this.battlefieldModel != null) {
            switch (type) {
                case GOLD:
                    return this.battlefieldModel.battleInfo.map.goldsRegions;
                case ARMOR:
                    return this.battlefieldModel.battleInfo.map.armorsRegions;
                case DAMAGE:
                    return this.battlefieldModel.battleInfo.map.damagesRegions;
                case NITRO:
                    return this.battlefieldModel.battleInfo.map.nitrosRegions;
                case HEALTH:
                    return this.battlefieldModel.battleInfo.map.healthsRegions;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    private final ArrayList<BonusRegion> usedRegions = new ArrayList<>();

    public void removeRegion(BonusRegion region) {
        usedRegions.remove(region);
    }

    private BonusRegion getSpawnPos(BonusType type, int index) {
        int currIndex = index;

        ArrayList<BonusRegion> regionType = getRegionType(type);
        if (currIndex >= regionType.size()) {
            return null;
        }
        BonusRegion spawnPosition = regionType.get(currIndex);

        for (BonusRegion usedBonusRegion : usedRegions) {
            if (spawnPosition == usedBonusRegion) {
                if (currIndex >= regionType.size()) {
                    return null;
                }
                currIndex++;
                return getSpawnPos(type, currIndex);
            }
        }
        // if(currIndex == 0 && type == BonusType.GOLD){
        // int randomIndex = getRandomIndex(regionType.size());
        // currIndex = randomIndex;
        // }
        usedRegions.add(regionType.get(currIndex));
        return regionType.get(currIndex);
    }

    private Vector3 getRandomSpawnPostiton(BonusRegion region) {
        Vector3 f = new Vector3(0.0f, 0.0f, 0.0f);
        f.x = (region.min.x + region.max.x) / 2;
        f.y = (region.min.y + region.max.y) / 2;
        f.z = region.max.z;
        return f;
    }

    private static int getRandomIndex(int size) {
        Random random = new Random();
        return random.nextInt(size);
    }

    public void spawnBonus(BonusType type) {
        BonusRegion region = null;
        Bonus bonus = null;
        ArrayList<BonusRegion> regionType = getRegionType(type);
        if (type == BonusType.GOLD) {
            if (this.battlefieldModel.battleInfo.map.goldsRegions.size() > 0) {
                region = getSpawnPos(type, 0);
                if (region == null) {
                    return;
                }
                this.battlefieldModel.sendToAllPlayers(flashtanki.commands.Type.BATTLE, "gold_spawn;SYSTEM");
                this.addGoldDroppzone(region, "gold" + this.inc);
                bonus = new Bonus(this.getSpawnPostitonGold(region), BonusType.GOLD, region,
                        "gold" + this.inc, this.inc);
                GoldSchedule goldSchedule = new GoldSchedule(this.battlefieldModel, bonus, this.inc);
            } else {
                loggerService.log(LogType.INFO, "Regions for gold do not exist");
            }
        } else {
            if (!(regionType.size() <= 0)) {
                region = getSpawnPos(type, 0);
                if (region == null) {
                    return;
                }
                bonus = new Bonus(this.getSpawnPostiton(region), type, region, "default", this.inc);
                this.battlefieldModel.spawnBonus(bonus, this.inc, 9999);
            } else {
                loggerService.log(LogType.INFO, "Regions for bonus do not exist");
            }
        }
        ++this.inc;
    }

    public void addGoldDroppzone(BonusRegion region, String id) {
        Float x = (region.max.x + region.min.x) / 2;
        Float y = (region.max.y + region.min.y) / 2;
        Float z = region.max.y + 2000;
        JSONObject obj = new JSONObject();
        JSONObject rayPos = new JSONObject();
        rayPos.put("x", x);
        rayPos.put("y", y);
        rayPos.put("z", z);
        JSONObject origin = new JSONObject();
        origin.put("x", x);
        origin.put("y", y);
        origin.put("z", z);
        obj.put("rayPos", rayPos);
        obj.put("origin", origin);
        obj.put("id", id);
        this.battlefieldModel.sendToAllPlayers(flashtanki.commands.Type.BATTLE,
                "create_graffiti;droppzone_gold;" + obj.toJSONString());
    }

    public void addGoldDroppzone(BonusRegion region, String id, BattlefieldPlayerController controller) {
        Float x = (region.max.x + region.min.x) / 2;
        Float y = (region.max.y + region.min.y) / 2;
        Float z = region.max.y + 2000;
        JSONObject obj = new JSONObject();
        JSONObject rayPos = new JSONObject();
        rayPos.put("x", x);
        rayPos.put("y", y);
        rayPos.put("z", z);
        JSONObject origin = new JSONObject();
        origin.put("x", x);
        origin.put("y", y);
        origin.put("z", z);
        obj.put("rayPos", rayPos);
        obj.put("origin", origin);
        obj.put("id", id);
        controller.send(flashtanki.commands.Type.BATTLE, "create_graffiti;droppzone_gold;" + obj.toJSONString());
    }

    public void addGoldDroppzone(BonusRegion region, String id, SpectatorController controller) {
        Float x = (region.max.x + region.min.x) / 2;
        Float y = (region.max.y + region.min.y) / 2;
        Float z = region.max.y + 2000;
        JSONObject obj = new JSONObject();
        JSONObject rayPos = new JSONObject();
        rayPos.put("x", x);
        rayPos.put("y", y);
        rayPos.put("z", z);
        JSONObject origin = new JSONObject();
        origin.put("x", x);
        origin.put("y", y);
        origin.put("z", z);
        obj.put("rayPos", rayPos);
        obj.put("origin", origin);
        obj.put("id", id);
        controller.sendCommand(flashtanki.commands.Type.BATTLE, "create_graffiti;droppzone_gold;" + obj.toJSONString());
    }

    public void displayAllDroppzones(BattlefieldPlayerController controller) {
        if (controller != null && this.battlefieldModel != null) {
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.nitrosRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.nitrosRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", z);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.send(flashtanki.commands.Type.BATTLE, "create_graffiti;droppzone_nitro;" + obj.toJSONString());
            }
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.armorsRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.armorsRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", z);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.send(flashtanki.commands.Type.BATTLE, "create_graffiti;droppzone_armorup;" + obj.toJSONString());
            }
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.damagesRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.damagesRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", z);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.send(flashtanki.commands.Type.BATTLE,
                        "create_graffiti;droppzone_damageup;" + obj.toJSONString());
            }
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.healthsRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.healthsRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", 0);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.send(flashtanki.commands.Type.BATTLE, "create_graffiti;droppzone_medkit;" + obj.toJSONString());
            }
        }
    }

    public void displayAllDroppzones(SpectatorController controller) {
        if (controller != null && this.battlefieldModel != null) {
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.nitrosRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.nitrosRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", z);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.sendCommand(flashtanki.commands.Type.BATTLE,
                        "create_graffiti;droppzone_nitro;" + obj.toJSONString());
            }
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.armorsRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.armorsRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", z);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.sendCommand(flashtanki.commands.Type.BATTLE,
                        "create_graffiti;droppzone_armorup;" + obj.toJSONString());
            }
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.damagesRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.damagesRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", z);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.sendCommand(flashtanki.commands.Type.BATTLE,
                        "create_graffiti;droppzone_damageup;" + obj.toJSONString());
            }
            for (int i = 0; i < this.battlefieldModel.battleInfo.map.healthsRegions.size(); i++) {
                BonusRegion region = this.battlefieldModel.battleInfo.map.healthsRegions.get(i);
                Float x = (region.min.x + region.max.x) / 2;
                Float y = (region.min.y + region.max.y) / 2;
                Float z = region.max.z + 2000;
                JSONObject obj = new JSONObject();
                JSONObject rayPos = new JSONObject();
                rayPos.put("x", x);
                rayPos.put("y", y);
                rayPos.put("z", 0);
                JSONObject origin = new JSONObject();
                origin.put("x", x);
                origin.put("y", y);
                origin.put("z", z);
                obj.put("rayPos", rayPos);
                obj.put("origin", origin);
                obj.put("id", 99999999);
                controller.sendCommand(flashtanki.commands.Type.BATTLE,
                        "create_graffiti;droppzone_medkit;" + obj.toJSONString());
            }
        }
    }

    public void battleFinished() {
        this.prevFund = 0;
        this.crystallFund = 0;
        this.goldFund = 0;
        this.nextGoldFund = (int) RandomUtils.getRandom(7000.0f, 7300.0f);
    }

    private Vector3 getSpawnPostiton(BonusRegion region) {
        Vector3 f = new Vector3(0.0f, 0.0f, 0.0f);
        f.x = (region.min.x + region.max.x) / 2;
        f.y = (region.min.y + region.max.y) / 2;
        f.z = region.max.z + 2000;
        return f;
    }

    private Vector3 getSpawnPostitonGold(BonusRegion region) {
        Vector3 f = new Vector3(0.0f, 0.0f, 0.0f);
        f.x = (region.min.x + region.max.x) / 2;
        f.y = (region.min.y + region.max.y) / 2;
        f.z = region.max.z + 2000;
        return f;
    }

    public void updatedFund() {
        int deff = (int) this.battlefieldModel.tanksKillModel.getBattleFund() - this.prevFund;
        this.goldFund += deff;
        this.crystallFund += deff;
        if (this.goldFund >= this.nextGoldFund) {
            this.spawnBonus(BonusType.GOLD);
            this.nextGoldFund = (int) RandomUtils.getRandom(7000.0f, 7300.0f);
            this.goldFund = 0;
        }
        this.prevFund = (int) this.battlefieldModel.tanksKillModel.getBattleFund();
    }

    public void sendAlreadyDroppedBonuses(BattlefieldPlayerController controller) {
        if (controller != null && this.battlefieldModel != null) {
            for (Map.Entry<String, Bonus> entry : this.battlefieldModel.activeBonuses.entrySet()) {
                String key = entry.getKey();
                Bonus bonus = entry.getValue();
                controller.send(flashtanki.commands.Type.BATTLE, "spawn_bonus_instant",
                        JSONUtils.parseBonusInfoOnJoin(bonus, bonus.inc, 9999));
                if (bonus.type == BonusType.GOLD) {
                    addGoldDroppzone(bonus.bonusRegion, bonus.id, controller);
                }
            }
        }
    }

    public void sendAlreadyDroppedBonuses(SpectatorController controller) {
        if (controller != null && this.battlefieldModel != null) {
            for (Map.Entry<String, Bonus> entry : this.battlefieldModel.activeBonuses.entrySet()) {
                String key = entry.getKey();
                Bonus bonus = entry.getValue();
                controller.sendCommand(flashtanki.commands.Type.BATTLE, "spawn_bonus_instant",
                        JSONUtils.parseBonusInfoOnJoin(bonus, bonus.inc, 9999));
                if (bonus.type == BonusType.GOLD) {
                    addGoldDroppzone(bonus.bonusRegion, bonus.id, controller);
                }
            }
        }
    }

    @Override
    public void run() {
        if (this.battlefieldModel.battleInfo.map.crystallsRegions.size() <= 0
                && this.battlefieldModel.battleInfo.map.goldsRegions.size() <= 0) {
            this.battlefieldModel = null;
        }
        while (this.battlefieldModel != null) {
            try {
                Thread.sleep(5000L);
                if (this.battlefieldModel == null || this.battlefieldModel.players == null)
                    break;
                this.spawnRandomBonus();
            } catch (InterruptedException e) {
                loggerService.log(LogType.ERROR, e.getMessage());
            }
        }
    }
}
