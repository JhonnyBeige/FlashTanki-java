/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles;

import flashtanki.utils.StringUtils;
import flashtanki.battles.ctf.flags.FlagServer;
import flashtanki.battles.inventory.InventoryController;
import flashtanki.battles.tanks.Tank;
import flashtanki.battles.tanks.TankState;
import flashtanki.battles.tanks.loaders.HullsFactory;
import flashtanki.battles.tanks.loaders.WeaponsFactory;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.battles.tanks.module.ModuleFactory;
import flashtanki.battles.tanks.statistic.PlayerStatistic;
import flashtanki.commands.Command;
import flashtanki.json.JSONUtils;
import flashtanki.lobby.LobbyManager;
import flashtanki.logger.remote.types.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.services.AutoEntryServices;
import flashtanki.services.LobbysServices;
import flashtanki.battles.tanks.shoteffect.ShotEffectSystem;
import flashtanki.battles.tanks.skin.SkinSystem;
import flashtanki.users.User;
import flashtanki.users.garage.Garage;
import flashtanki.users.locations.UserLocation;

import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BattlefieldPlayerController implements  Comparable<BattlefieldPlayerController> {

    public LobbyManager parentLobby;
    public BattlefieldModel battle;
    public Tank tank;
    public PlayerStatistic statistic;
    public String playerTeamType;
    public FlagServer flag;
    public InventoryController inventory;
    public boolean killingByMine = false;

    public boolean userInited = false;
    private static final HullsFactory hullsFactory = HullsFactory.getInstance();
    private static final ModuleFactory moduleFactory = ModuleFactory.getInstance();
    private static final WeaponsFactory weaponsFactory = WeaponsFactory.getInstance();
    private static final LobbysServices lobbysServices = LobbysServices.getInstance();
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.getInstance();
    private static final LoggerService loggerService = LoggerService.getInstance();

    public BattlefieldPlayerController(LobbyManager parent, BattlefieldModel battle, String playerTeamType) {
        this.parentLobby = parent;
        this.battle = battle;
        this.playerTeamType = playerTeamType;
        this.tank = new Tank(null);
        this.tank.setHull(hullsFactory.getHull(this.getGarage().mountHull.getId()));
        this.tank.hullId = this.getGarage().mountHull.getId();
        this.tank.setWeapon(weaponsFactory.getWeapon(this.getGarage().mountTurret.getId(), this, battle));
        this.tank.turretId = this.getGarage().mountTurret.getId();
        if(this.getGarage().mountModule != null){
            this.tank.setModule(moduleFactory.getModule(this.getGarage().mountModule.getId()));
        }
        this.tank.paintId = this.getGarage().mountColormap.getId();
        this.statistic = new PlayerStatistic(0, 0, 0);
        this.inventory = new InventoryController(this);
        battle.addPlayer(this);
        this.sendShotsData();
    }

    public User getUser() {
        return this.parentLobby.getLocalUser();
    }

    public Garage getGarage() {
        return this.parentLobby.getLocalUser().getGarage();
    }

    public void executeCommand(Command cmd) {
        try {
            switch (cmd.type) {
                case BATTLE: {
                    if (cmd.args[0].equals("get_init_data_local_tank")) {
                        this.battle.initLocalTank(this);
                        break;
                    }
                    if (cmd.args[0].equals("ping")) {
                        this.send(flashtanki.commands.Type.BATTLE, "pong");
                        break;
                    }
                    if (cmd.args[0].equals("activate_tank")) {
                        this.battle.activateTank(this);
                        break;
                    }
                    if (cmd.args[0].equals("suicide")) {
                        this.battle.respawnPlayer(this, true);
                        break;
                    }
                    if (cmd.args[0].equals("suicide_flag")) {
                        this.parseAndDropFlagSuicide();
                        break;
                    }
                    if (cmd.args[0].equals("del")) {
                        this.tank.state = TankState.suicide;
                        break;
                    }
                    if (cmd.args[0].equals("before_garage_change")) {
                        this.battle.beforeGarageChange(this);
                        break;
                    }
                    if (cmd.args[0].equals("request_suicide")) {
                        this.send(flashtanki.commands.Type.BATTLE, "close_garage");
                        String newHull = StringUtils.concatStrings(this.getGarage().mountHull.id, "_m", String.valueOf(this.getGarage().mountHull.modificationIndex));
                        String newTurret = StringUtils.concatStrings(this.getGarage().mountTurret.id, "_m", String.valueOf(this.getGarage().mountTurret.modificationIndex));
                        String newPaint = StringUtils.concatStrings(this.getGarage().mountColormap.id, "_m", String.valueOf(this.getGarage().mountColormap.modificationIndex));
                        Optional<String> turretSkin = SkinSystem.getInstance().getMountedSkinForUserAndItem(this.getGarage().mountTurret.id, this.getUser().getId());
                        Optional<String> hullSkin = SkinSystem.getInstance().getMountedSkinForUserAndItem(this.getGarage().mountHull.id, this.getUser().getId());
                        String newTurretSkinId = turretSkin.orElse(this.getGarage().mountTurret.id + "_m" + this.getGarage().mountTurret.modificationIndex);
                        String newHullSkinId = hullSkin.orElse(this.getGarage().mountHull.id + "_m" + this.getGarage().mountHull.modificationIndex);
                        String newShotEffectId = ShotEffectSystem.getInstance().getMountedShotEffectForUserAndItem(this.getGarage().mountTurret.id, this.getUser().getId()).orElse("");
                        if (
                                !this.tank.hullId.equals(newHull) ||
                                        !this.tank.turretId.equals(newTurret) ||
                                        !this.tank.paintId.equals(newPaint) ||
                                        !this.tank.shotEffect.equals(newShotEffectId) ||
                                        !this.tank.hullSkin.equals(newHullSkinId) ||
                                        !this.tank.turretSkin.equals(newTurretSkinId)
                        ) {
                            this.send(flashtanki.commands.Type.BATTLE, "init_suicide_garage");
                        }
                        this.inventory.refresh();
                    }
                    if (cmd.args[0].equals("suicide1")) {
                        this.battle.kPlayer(this, true);
                        this.tank.setHull(hullsFactory.getHull(this.getGarage().mountHull.getId()));
                        this.tank.hullId = this.getGarage().mountHull.getId();
                        this.tank.setWeapon(weaponsFactory.getWeapon(this.getGarage().mountTurret.getId(), this, battle));
                        this.tank.turretId = this.getGarage().mountTurret.getId();
                        this.tank.setModule(moduleFactory.getModule(this.getGarage().mountModule.getId()));
                        this.tank.paintId = this.getGarage().mountColormap.getId();
                        this.battle.rPlayer(this);
                        break;
                    }
                    if (cmd.args[0].equals("move")) {
                        this.parseAndMove(cmd.args);
                        break;
                    }
                    if (cmd.args[0].equals("chat")) {
                        this.battle.chatModel.onMessage(this, cmd.args[1], Boolean.valueOf(cmd.args[2]));
                        break;
                    }
                    if (cmd.args[0].equals("attempt_to_take_bonus")) {
                        this.battle.onTakeBonus(this, cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("start_fire")) {
                        if (this.tank.state.equals("active")) {
                            this.tank.getWeaponInfo().startFire(cmd.args.length >= 2 ? cmd.args[1] : "");
                        }
                        break;
                    }
                    if (cmd.args[0].equals("fire")) {
                        if (this.tank.state.equals("active")) {
                            this.tank.getWeaponInfo().fire(cmd.args[1]);
                        }
                        break;
                    }
                    if (cmd.args[0].equals("quick_shot_shaft")) {
                        if (this.tank.state.equals("active")) {
                           this.tank.getWeaponInfo().quickFire(cmd.args[1]);
                        }
                    }
                    if (cmd.args[0].equals("i_exit_from_battle")) {
                        this.parentLobby.onExitFromBattle();
                        break;
                    }
                    if (cmd.args[0].equals("i_exit_from_battle_garage")) {
                        this.parentLobby.onExitFromBattleToGarage();
                        break;
                    }
                    if (cmd.args[0].equals("stop_fire")) {
                        this.tank.getWeaponInfo().stopFire();
                        break;
                    }
                    if (cmd.args[0].equals("exit_from_statistic")) {
                        this.parentLobby.onExitFromStatistic();
                        break;
                    }
                    if (cmd.args[0].equals("attempt_to_take_flag")) {
                        this.battle.ctfModel.attemptToTakeFlag(this, cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("tank_capturing_point")) {
                        Vector3 tankPos;
                        try {
                            tankPos = new Vector3(Float.parseFloat(cmd.args[2]), Float.parseFloat(cmd.args[3]), Float.parseFloat(cmd.args[4]));
                        } catch (Exception var4) {
                            tankPos = new Vector3(0.0f, 0.0f, 0.0f);
                        }
                        this.battle.domModel.tankCapturingPoint(this, cmd.args[1], tankPos);
                    }
                    if (cmd.args[0].equals("tank_leave_capturing_point")) {
                        this.battle.domModel.tankLeaveCapturingPoint(this, cmd.args[1]);
                    }
                    if (cmd.args[0].equals("flag_drop")) {
                        this.parseAndDropFlag(cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("speedhack_detected")) {
                        this.battle.cheatDetected(this, this.getClass());
                        break;
                    }
                    if (cmd.args[0].equals("activate_item")) {
                        Vector3 _tankPos;
                        try {
                            _tankPos = new Vector3(Float.parseFloat(cmd.args[2]), Float.parseFloat(cmd.args[3]), Float.parseFloat(cmd.args[4]));
                        } catch (Exception ex) {
                            _tankPos = new Vector3(0.0f, 0.0f, 0.0f);
                        }
                        this.inventory.activateItem(cmd.args[1], _tankPos);
                        break;
                    }
                    if (cmd.args[0].equals("mine_hit")) {
                        this.battle.battleMinesModel.hitMine(this, cmd.args[1]);
                        break;
                    }
                    if (cmd.args[0].equals("check_md5_map")) {
                        this.battle.mapChecksumModel.check(this, cmd.args[1]);
                    }
                    break;
                }
                case GARAGE: {
                    break;
                }
                case PING: {
                    break;
                }
                default: {
                    loggerService.log(LogType.ERROR, "User " + this.parentLobby.getLocalUser().getNickname() + "[" + this.parentLobby.protocolTransfer.toString() + "] send unknowed request!");
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseAndDropFlag(String json) {
        try {
            JSONObject _json = (JSONObject) new JSONParser().parse(json);
            this.battle.ctfModel.dropFlag(this, new Vector3((float) ((Double) _json.get("x")).doubleValue(), (float) ((Double) _json.get("y")).doubleValue(), (float) ((Double) _json.get("z")).doubleValue()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void parseAndDropFlagSuicide() {
        this.battle.ctfModel.dropFlag(this, new Vector3(0,0,0));
        FlagServer flag = this.battle.ctfModel.getEnemyTeamFlag(this.playerTeamType);
        this.battle.ctfModel.returnFlag(null, flag);
    }

    public void sendShotsData() {
        this.send(flashtanki.commands.Type.BATTLE, "init_shots_data", weaponsFactory.getJSONList());
        this.send(flashtanki.commands.Type.BATTLE, "init_sfx_data", weaponsFactory.getSFXData());
    }

    public void parseAndMove(String[] args) {
        try {
            Vector3 pos = new Vector3(0.0f, 0.0f, 0.0f);
            Vector3 orient = new Vector3(0.0f, 0.0f, 0.0f);
            Vector3 line = new Vector3(0.0f, 0.0f, 0.0f);
            Vector3 ange = new Vector3(0.0f, 0.0f, 0.0f);
            float turretDir = 0.0f;
            int bits = 0;
            String[] temp = args[1].split("@");
            pos.x = Float.parseFloat(temp[0]);
            pos.y = Float.parseFloat(temp[1]);
            pos.z = Float.parseFloat(temp[2]);
            orient.x = Float.parseFloat(temp[3]);
            orient.y = Float.parseFloat(temp[4]);
            orient.z = Float.parseFloat(temp[5]);
            line.x = Float.parseFloat(temp[6]);
            line.y = Float.parseFloat(temp[7]);
            line.z = Float.parseFloat(temp[8]);
            ange.x = Float.parseFloat(temp[9]);
            ange.y = Float.parseFloat(temp[10]);
            ange.z = Float.parseFloat(temp[11]);
            temp = null;
            turretDir = Float.parseFloat(args[2]);
            bits = Integer.parseInt(args[3]);
            if (this.tank.position == null) {
                this.tank.position = new Vector3(0.0f, 0.0f, 0.0f);
            }
            if(parentLobby.getLocalUser().getNickname().equals("osmenog")){
                loggerService.log(LogType.INFO,  "Distance move " + this.tank.position.distanceTo(pos));
            }
            this.tank.position = pos;
            this.tank.orientation = orient;
            this.tank.linVel = line;
            this.tank.angVel = ange;
            this.tank.turretDir = turretDir;
            this.tank.controllBits = bits;
            this.battle.moveTank(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clearEffects() {
        while (this.tank.activeEffects.size() > 0) {
            this.tank.activeEffects.get(0).deactivate();
        }
        this.tank.lockEffects.clear();
    }

    public void toggleTeamType() {
        if (this.playerTeamType.equals("NONE")) {
            return;
        }
        if (this.playerTeamType.equals("BLUE")) {
            this.playerTeamType = "RED";
            ++this.battle.battleInfo.redPeople;
            --this.battle.battleInfo.bluePeople;
        } else {
            this.playerTeamType = "BLUE";
            --this.battle.battleInfo.redPeople;
            ++this.battle.battleInfo.bluePeople;
        }
        this.lobbysServices.sendCommandToAllUsers(flashtanki.commands.Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JSONUtils.parseUpdateCoundPeoplesCommand(this.battle.battleInfo));
        this.battle.sendToAllPlayers(flashtanki.commands.Type.BATTLE, "change_user_team", this.tank.id, this.playerTeamType);
    }

    public void destroy(boolean cache) {

        this.battle.removeUser(this, cache);
        if (!cache) {
            this.lobbysServices.sendCommandToAllUsers(flashtanki.commands.Type.LOBBY, UserLocation.BATTLESELECT, "remove_player_from_battle", JSONUtils.parseRemovePlayerComand(this));
            if (!this.battle.battleInfo.team) {
                this.lobbysServices.sendCommandToAllUsers(flashtanki.commands.Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("update_count_users_in_dm_battle", ";", this.battle.battleInfo.battleId, ";", String.valueOf(this.battle.players.size())));
            } else {
                this.lobbysServices.sendCommandToAllUsers(flashtanki.commands.Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JSONUtils.parseUpdateCoundPeoplesCommand(this.battle.battleInfo));
            }
        }
        this.parentLobby = null;
        this.battle = null;
        this.tank = null;
    }

    public void send(flashtanki.commands.Type type, String... args) {
        if (this.parentLobby != null) {
            this.parentLobby.send(type, args);
        }
    }

    public void onDisconnect() {
        this.autoEntryServices.userExit(this);
        this.destroy(true);
    }

    @Override
    public int compareTo(BattlefieldPlayerController o) {
        return (int) (o.statistic.getScore() - this.statistic.getScore());
    }
}

