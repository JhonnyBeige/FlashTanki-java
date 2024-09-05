/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.ctf;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.ctf.anticheats.CaptureTheFlagAnticheatModel;
import flashtanki.battles.ctf.flags.FlagServer;
import flashtanki.battles.ctf.flags.FlagState;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.services.TanksServices;
import java.util.ArrayList;

public class CTFModel
extends CaptureTheFlagAnticheatModel {
    private BattlefieldModel bfModel;
    private FlagServer blueFlag = new FlagServer();
    private FlagServer redFlag = new FlagServer();
    private TanksServices tanksServices = TanksServices.getInstance();

    public CTFModel(BattlefieldModel bfModel) {
        super(bfModel);
        this.bfModel = bfModel;
        this.blueFlag.flagTeamType = "BLUE";
        this.redFlag.flagTeamType = "RED";
        this.blueFlag.state = FlagState.BASE;
        this.redFlag.state = FlagState.BASE;
        this.blueFlag.basePosition = this.blueFlag.position = bfModel.battleInfo.map.flagBluePosition;
        this.redFlag.basePosition = this.redFlag.position = bfModel.battleInfo.map.flagRedPosition;
    }

    public void attemptToTakeFlag(BattlefieldPlayerController taker, String flagTeamType) {
        FlagServer flag = this.getTeamFlag(flagTeamType);
        if (flag.owner != null) {
            return;
        }
        if(!taker.tank.state.equals("active")){
            return;
        }
        if (taker.playerTeamType.equals(flagTeamType)) {
            FlagServer enemyFlag = this.getEnemyTeamFlag(flagTeamType);
            if (flag.state == FlagState.DROPED) {
                this.returnFlag(taker, flag);
                return;
            }
            if (enemyFlag.owner == taker) {
                if (this.onDeliveredFlag(taker, enemyFlag)) {
                    return;
                }
                this.bfModel.sendToAllPlayers(Type.BATTLE, "deliver_flag", taker.playerTeamType, taker.tank.id);
                enemyFlag.state = FlagState.BASE;
                enemyFlag.owner = null;
                taker.flag = null;
                if (enemyFlag.returnTimer != null) {
                    enemyFlag.returnTimer.stop = true;
                    enemyFlag.returnTimer = null;
                }
                int score = (taker.playerTeamType == "BLUE" ? this.bfModel.battleInfo.redPeople : this.bfModel.battleInfo.bluePeople) * 10;
                this.tanksServices.addScore(taker.parentLobby, score);
                this.bfModel.statistics.changeStatistic(taker);
                double fund = 0.0;
                ArrayList<BattlefieldPlayerController> otherTeam = new ArrayList<BattlefieldPlayerController>();
                for (BattlefieldPlayerController player : this.bfModel.players) {
                    if (player.playerTeamType.equals(taker.playerTeamType) || player.playerTeamType.equals("NONE")) continue;
                    otherTeam.add(player);
                }
                for (BattlefieldPlayerController otherPlayer : otherTeam) {
                    fund += Math.sqrt((double)otherPlayer.getUser().getRang() * 0.125);
                }
                this.bfModel.tanksKillModel.addFund(fund);
                if (taker.playerTeamType == "BLUE") {
                    ++this.bfModel.battleInfo.scoreBlue;
                    this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "BLUE", String.valueOf(this.bfModel.battleInfo.scoreBlue));
                    if (this.bfModel.battleInfo.numFlags == this.bfModel.battleInfo.scoreBlue) {
                        this.bfModel.tanksKillModel.restartBattle(false);
                    }
                } else {
                    ++this.bfModel.battleInfo.scoreRed;
                    this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "RED", String.valueOf(this.bfModel.battleInfo.scoreRed));
                    if (this.bfModel.battleInfo.numFlags == this.bfModel.battleInfo.scoreRed) {
                        this.bfModel.tanksKillModel.restartBattle(false);
                    }
                }
            }
        } else {
            if (this.onTakeFlag(taker, flag)) {
                return;
            }
            this.bfModel.sendToAllPlayers(Type.BATTLE, "flagTaken", taker.tank.id, flagTeamType);
            flag.state = FlagState.TAKEN_BY;
            flag.owner = taker;
            taker.flag = flag;
            if (flag.returnTimer != null) {
                flag.returnTimer.stop = true;
                flag.returnTimer = null;
            }
        }
    }

    public void dropFlag(BattlefieldPlayerController following, Vector3 posDrop) {
        FlagServer flag = this.getEnemyTeamFlag(following.playerTeamType);
        flag.state = FlagState.DROPED;
        flag.position = posDrop;
        flag.owner = null;
        following.flag = null;
        flag.returnTimer = new FlagReturnTimer(this, flag);
        flag.returnTimer.start();
        this.bfModel.sendToAllPlayers(Type.BATTLE, "flag_drop", JSONUtils.parseDropFlagCommand(flag));
    }

    public void returnFlag(BattlefieldPlayerController following, FlagServer flag) {
        flag.state = FlagState.BASE;
        if (flag.owner != null) {
            flag.owner.flag = null;
            flag.owner = null;
        }
        flag.position = flag.basePosition;
        if (flag.returnTimer != null) {
            flag.returnTimer.stop = true;
            flag.returnTimer = null;
        }
        String id = following == null ? null : following.tank.id;
        this.bfModel.sendToAllPlayers(Type.BATTLE, "return_flag", flag.flagTeamType, id);
        int score = 0;//5; FIXME: check how close to flag it is and give from 0 to 5 xp
        if (following != null) {
            this.tanksServices.addScore(following.parentLobby, score);
            this.bfModel.statistics.changeStatistic(following);
        }
    }

    private FlagServer getTeamFlag(String teamType) {
        if (teamType.equals("BLUE")) {
            return this.blueFlag;
        }
        if (teamType.equals("RED")) {
            return this.redFlag;
        }
        return null;
    }

    public FlagServer getEnemyTeamFlag(String teamType) {
        if (teamType.equals("BLUE")) {
            return this.redFlag;
        }
        if (teamType.equals("RED")) {
            return this.blueFlag;
        }
        return null;
    }

    public FlagServer getRedFlag() {
        return this.redFlag;
    }

    public FlagServer getBlueFlag() {
        return this.blueFlag;
    }
}

