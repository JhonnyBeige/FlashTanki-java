/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flashtanki.utils.StringUtils;
import flashtanki.battles.dom.DominationPoint;
import flashtanki.battles.effects.EffectType;
import flashtanki.battles.tanks.Tank;
import flashtanki.battles.tanks.TankState;
import flashtanki.battles.tanks.data.DamageTankData;
import flashtanki.battles.tanks.loaders.WeaponsFactory;
import flashtanki.battles.tanks.statistic.prizes.BattlePrizeCalculate;
import flashtanki.battles.tanks.weapons.WeaponUtils;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.main.kafka.KafkaTemplateService;
import flashtanki.lobby.battles.BattleInfo;
import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.users.premium.PremiumService;
import flashtanki.services.TanksServices;
import flashtanki.system.destroy.Destroyable;
import flashtanki.system.quartz.QuartzService;
import flashtanki.system.quartz.TimeType;
import flashtanki.system.quartz.impl.QuartzServiceImpl;
import flashtanki.users.garage.items.Item;

import java.util.*;

public class TankKillModel
        implements Destroyable {
    private static final String QUARTZ_GROUP = TankKillModel.class.getName();
    private final String QUARTZ_NAME;
    private final BattlefieldModel bfModel;
    private final TanksServices tanksServices = TanksServices.getInstance();
    private final QuartzService quartzService = QuartzServiceImpl.getInstance();
    private final BattlePrizeCalculate prizeCalculate = new BattlePrizeCalculate();
    private final KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private final PremiumService premiumService = PremiumService.getInstance();
    private double battleFund;
    private final BattleInfo battleInfo;
    private String ADDED_BATTLE_SCORE_TOPIC = "added-battle-score-request";

    public TankKillModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
        this.battleInfo = bfModel.battleInfo;
        this.QUARTZ_NAME = "TankKillModel " + this.hashCode() + " battle=" + this.battleInfo.battleId;
    }

    public synchronized void damageTank(BattlefieldPlayerController controller, BattlefieldPlayerController damager,
                                        float damage, boolean considerDD) {
        if (controller == null || damager == null) {
            return;
        }
        Tank tank = controller.tank;
        if (tank.state.equals("newcome") || tank.state.equals("suicide")) {
            return;
        }
        if (this.bfModel.battleInfo.team && controller != damager
                && controller.playerTeamType.equals(damager.playerTeamType) && !this.bfModel.battleInfo.friendlyFire) {
            return;
        }

        float microUpgradesDamage = 0;
        for (final Item item : damager.parentLobby.getLocalUser().getGarage().items) {
            if (item.getId().equals(damager.getGarage().mountTurret.getId())) {
                int microUpgrades = damager.getGarage().mountTurret.microUpgrades;
                int microUpgradesMax = 10;
                String itemId = StringUtils.concatStrings(item.id, "_m", Integer.toString(item.modificationIndex));
                float currentMaxDamage = WeaponsFactory.getInstance().getWeaponDamageData(itemId).damage_max;
                float nextModificationDamage;
                float nextUpgradeDifference;
                if (item.modificationIndex == 3) {
                    String itemIdMinusMod = itemId.replace("m3", "m2");
                    nextModificationDamage = WeaponsFactory.getInstance()
                            .getWeaponDamageData(itemIdMinusMod).damage_max;
                    nextUpgradeDifference = currentMaxDamage - nextModificationDamage;
                } else {
                    String[] itemData = itemId.split("_m");
                    String nextItemModification = StringUtils.concatStrings(itemData[0],
                            String.valueOf(Integer.parseInt(itemData[1]) + 1));
                    nextModificationDamage = WeaponsFactory.getInstance()
                            .getWeaponDamageData(nextItemModification).damage_max;
                    nextUpgradeDifference = nextModificationDamage - currentMaxDamage;
                }
                if (microUpgrades == 0) {
                    microUpgradesDamage = 0;
                } else {
                    microUpgradesDamage = calculateNextUpgrade(nextUpgradeDifference, microUpgrades - 1,
                            microUpgradesMax);
                }
            }
        }
        if (!damager.battle.battleInfo.microUpgrades) {
            microUpgradesDamage = 0;
        }

        Integer resistance = controller.tank.getModule() != null ?
                controller.tank.getModule().getResistance(damager.tank.getWeaponInfo().getEntity().getType()) : 0;
        if (controller.battle.battleInfo.battleFormat != 0) {
            resistance = 0;
        }
        damage = WeaponUtils.calculateDamageWithResistance((damage + microUpgradesDamage),
                resistance == null ? 0 : resistance);

        if (tank.isUsedEffect(EffectType.ARMOR)) {
            damage /= 2.0f;
            microUpgradesDamage /= 2.0f;
        }
        if (damager.tank.isUsedEffect(EffectType.DAMAGE) && considerDD) {
            damage *= 2.0f;
            microUpgradesDamage *= 2.0f;
        }

        DamageTankData lastDamage = tank.lastDamagers.get(damager);
        DamageTankData damageData = new DamageTankData();

        damageData.damage = damage;
        damageData.timeDamage = System.currentTimeMillis();
        damageData.damager = damager;

        if (lastDamage != null) {
            damageData.damage += lastDamage.damage;
        }
        if (damager.tank.isUsedEffect(EffectType.DAMAGE) && considerDD) {
            damageData.damage /= 2.0f;
        }
        if (controller != damager) {
            tank.lastDamagers.put(damager, damageData);
        }

        tank.healthPoints -= WeaponUtils.calculateHealth(tank, damage);
        this.changeHealth(tank, tank.healthPoints);

        int killedTank = 0;

        if (tank.healthPoints <= 0) {
            tank.healthPoints = 0;
            this.killTank(controller, damager);
            killedTank = 1;
        } else {
            damager.killingByMine = false;
        }

        if (damager != controller) {
            damager.send(Type.BATTLE,
                    "damage_tank;" + controller.parentLobby.getLocalUser().getNickname() + ";"
                            + String.valueOf(damage) + ";" + String.valueOf(killedTank) + ";"
                            + String.valueOf(killedTank));
        }
    }

    public static float calculateNextUpgrade(float propertyProgress, int progress, int progressMax) {
        int[] upgradesGraph = {8, 12, 17, 18, 20, 10, 6, 4, 3, 2};
        progress++;
        if (progress == 11) {
            progress--;
        }

        float totalUpgradePercentage = 0;
        for (int i = 0; i < progress; i++) {
            totalUpgradePercentage += upgradesGraph[i];
        }

        float roundedValue = Math.round(propertyProgress * (totalUpgradePercentage / 100) * 1000);
        float value = roundedValue / 1000;

        return value;
    }

    public boolean healPlayer(BattlefieldPlayerController healer, BattlefieldPlayerController target, float addHeal) {
        Tank targetTank = target.tank;
        if (targetTank.state.equals("newcome") || targetTank.state.equals("suicide")) {
            return false;
        }
        if (targetTank.healthPoints >= Tank.MAX_HEALTH_TANK) {
            return false;
        }
        targetTank.healthPoints += WeaponUtils.calculateHealth(targetTank, addHeal);
        if (targetTank.healthPoints >= Tank.MAX_HEALTH_TANK) {
            targetTank.healthPoints = Tank.MAX_HEALTH_TANK;
        }
        this.changeHealth(targetTank, targetTank.healthPoints);
        return true;
    }

    public void changeHealth(Tank tank, int value) {
        if (tank != null) {
            tank.healthPoints = value;
            this.bfModel.sendToAllPlayers(Type.BATTLE, "change_health", tank.id, String.valueOf(tank.healthPoints));
        }
    }

    public synchronized void killTank(BattlefieldPlayerController controller, BattlefieldPlayerController killer) {
        Tank tank = controller.tank;
        tank.state = TankState.suicide;
        tank.getWeaponInfo().stopFire();
        controller.clearEffects();
        controller.send(Type.BATTLE, "local_user_killed");
        if (killer == null) {
            this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", tank.id, "suicide");
        } else {
            if (killer.killingByMine) {
                this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", tank.id, "mine", killer.tank.id);
                killer.killingByMine = false;
            } else {
                this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", tank.id, "killed", killer.tank.id);
            }
            if (controller == killer) {
                controller.statistic.addDeaths();
                this.bfModel.statistics.changeStatistic(controller);
            } else {
                if (this.bfModel.battleInfo.team) {
                    if (controller.playerTeamType.equals(killer.playerTeamType)) {
                        if (this.bfModel.battleInfo.friendlyFire) {
                            controller.statistic.addDeaths();
                            this.bfModel.statistics.changeStatistic(controller);
                        }
                    } else {
                        killer.statistic.addKills();
                        controller.statistic.addDeaths();
                        if (killer.playerTeamType.equals("BLUE")) {
                            if (!this.battleInfo.battleType.equals("CTF")
                                    && !this.battleInfo.battleType.equals("DOM")) {
                                ++this.bfModel.battleInfo.scoreBlue;
                            }
                            if (this.battleInfo.battleType.equals("TDM")) {
                                this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "BLUE",
                                        String.valueOf(this.bfModel.battleInfo.scoreBlue));
                            }
                        } else if (killer.playerTeamType.equals("RED")) {
                            if (!this.battleInfo.battleType.equals("CTF")
                                    && !this.battleInfo.battleType.equals("DOM")) {
                                ++this.bfModel.battleInfo.scoreRed;
                            }
                            if (this.battleInfo.battleType.equals("TDM")) {
                                this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "RED",
                                        String.valueOf(this.bfModel.battleInfo.scoreRed));
                            }
                        }
                        this.bfModel.statistics.changeStatistic(killer);
                        this.bfModel.statistics.changeStatistic(controller);
                    }
                } else {
                    killer.statistic.addKills();
                    controller.statistic.addDeaths();
                    this.bfModel.statistics.changeStatistic(killer);
                    this.bfModel.statistics.changeStatistic(controller);
                }
                if (this.bfModel.battleInfo.team) {
                    LinkedHashMap<BattlefieldPlayerController, DamageTankData> lastDamagers = controller.tank.lastDamagers;
                    if (lastDamagers.size() <= 1) {
                        if (!controller.playerTeamType.equals(killer.playerTeamType)) {
                            this.tanksServices.addScore(killer.parentLobby, 10);
                        }
                        if (this.bfModel.battleInfo.team) {
                            this.bfModel.statistics.changeStatistic(killer);
                        }
                    } else {
                        DamageTankData damager1 = (DamageTankData) ((HashMap) lastDamagers)
                                .get(((HashMap) lastDamagers).keySet().toArray()[lastDamagers.size() - 1]);
                        DamageTankData damager2 = (DamageTankData) ((HashMap) lastDamagers)
                                .get(((HashMap) lastDamagers).keySet().toArray()[lastDamagers.size() - 2]);
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - damager1.timeDamage <= 10000L
                                && currentTime - damager2.timeDamage <= 10000L) {
                            int score1 = 0;
                            int score2 = 0;
                            if (damager1.damage > damager2.damage) {
                                score1 = (int) (0.15
                                        * (double) (100.0f / (controller.tank.getHullInfo().hp / damager1.damage)));
                                score2 = 15 - score1;
                            } else if (damager2.damage > damager1.damage) {
                                score2 = (int) (0.15
                                        * (double) (100.0f / (controller.tank.getHullInfo().hp / damager2.damage)));
                                score1 = 15 - score2;
                            } else {
                                score1 = 7;
                                score2 = 7;
                            }
                            score1 = Math.abs(score1);
                            score2 = Math.abs(score2);
                            if (!(controller.playerTeamType.equals(killer.playerTeamType)
                                    && controller.playerTeamType.equals(killer.playerTeamType))) {
                                this.tanksServices.addScore(damager1.damager.parentLobby, score1);
                                this.tanksServices.addScore(damager2.damager.parentLobby, score2);
                            }
                            if (this.bfModel.battleInfo.team) {
                                this.bfModel.statistics.changeStatistic(damager1.damager);
                                this.bfModel.statistics.changeStatistic(damager2.damager);
                            }
                        } else {
                            this.tanksServices.addScore(killer.parentLobby, 10);
                            this.bfModel.statistics.changeStatistic(killer);
                        }
                    }
                } else {
                    int killScore = 10;
                    if (controller.flag != null) {
                        killScore *= 2;
                    }
                    this.tanksServices.addScore(killer.parentLobby, killScore);
                }
                this.addFund(0.037 * (double) (killer.getUser().getRang() + 1) + 0.01);
            }
            if (this.battleInfo.numKills > 0 && killer.statistic.getKills() >= (long) this.battleInfo.numKills) {
                this.restartBattle(false);
            }
            if (controller.flag != null) {
                this.bfModel.ctfModel.dropFlag(controller, controller.tank.position);
            }
            if (this.bfModel.domModel != null) {
                String pointId = null;
                for (DominationPoint point : this.bfModel.domModel.getPoints()) {
                    if (!point.getUserIds().contains(controller.getUser().getNickname()))
                        continue;
                    pointId = point.getId();
                    break;
                }
                if (pointId != null) {
                    this.bfModel.domModel.tankLeaveCapturingPoint(controller, pointId);
                }
            }
        }
        this.bfModel.statistics.changeStatistic(controller);
        this.bfModel.respawnPlayer(controller, false);
        controller.tank.lastDamagers.clear();
    }

    public void restartBattle(boolean timeLimitFinish) {
        if (!timeLimitFinish && this.battleInfo.time > 0) {
            this.quartzService.deleteJob(this.bfModel.QUARTZ_NAME, BattlefieldModel.QUARTZ_GROUP);
        }
        this.calculatePrizes();
        this.bfModel.battleFinish();
        this.bfModel.sendToAllPlayers(Type.BATTLE, "battle_finish",
                JSONUtils.parseFishishBattle(this.bfModel.players, 10000));
        for (BattlefieldPlayerController player : this.bfModel.players.values()) {
            if (player == null) continue;
            player.statistic.clear();
        }
        this.quartzService.addJob(this.QUARTZ_NAME, QUARTZ_GROUP, e -> this.bfModel.battleRestart(), TimeType.MS,
                10000L);
        if (this.bfModel.domModel != null) {
            this.bfModel.domModel.restartBattle();
        }
    }

    private void calculatePrizes() {
        if (this.bfModel == null || this.bfModel.players == null || this.bfModel.players.size() <= 0) {
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<BattlefieldPlayerController> users = new ArrayList<BattlefieldPlayerController>(
                this.bfModel.players.values());
        users.forEach(player -> {
            long battleScore = player.statistic.getScore();
            int multiplier = premiumService.getPremiumTime(player.getUser().getId()).isActivated() ? 2 : 1;
            battleScore *= multiplier;
            long userId = player.getUser().getId();
            try {
                String message = objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "score", battleScore));
                if(battleScore<=0){
                    RemoteDatabaseLogger.error( new Exception("Score value must be greater than 0"));
                }else {
                    //FIXME: no kafka
                    //kafkaTemplateService.getProducer().send(message, ADDED_BATTLE_SCORE_TOPIC);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        if (!this.bfModel.battleInfo.team) {
            Collections.sort(users);
            prizeCalculate.calc(users, (int) this.battleFund);
        } else {
            ArrayList<BattlefieldPlayerController> redTeam = new ArrayList<BattlefieldPlayerController>();
            ArrayList<BattlefieldPlayerController> blueTeam = new ArrayList<BattlefieldPlayerController>();
            for (BattlefieldPlayerController player : users) {
                if (player.playerTeamType.equals("RED")) {
                    redTeam.add(player);
                    continue;
                }
                if (!player.playerTeamType.equals("BLUE"))
                    continue;
                blueTeam.add(player);
            }
            prizeCalculate.calculateForTeam(redTeam, blueTeam, this.bfModel.battleInfo.scoreRed,
                    this.bfModel.battleInfo.scoreBlue, 0.25, (int) this.battleFund);
        }
    }

    public double fundMultiplier = 1;

    public void addFund(double value) {
        this.battleFund += value * 5 * fundMultiplier;
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_fund", String.valueOf((int) this.battleFund));
        if ((this.bfModel.battleInfo.isPaid && this.bfModel.battleInfo.inventory) || !this.bfModel.battleInfo.isPaid) {
            this.bfModel.bonusesSpawnService.updatedFund();
        }
    }

    public double getBattleFund() {
        return this.battleFund;
    }

    public void setBattleFund(int value) {
        this.battleFund = value;
    }

    @Override
    public void destroy() {
        this.quartzService.deleteJob(this.QUARTZ_NAME, QUARTZ_GROUP);
    }

}
