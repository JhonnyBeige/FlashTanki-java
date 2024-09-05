/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.spectator;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.tanks.loaders.WeaponsFactory;
import flashtanki.commands.Command;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.lobby.LobbyManager;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.users.User;

public class SpectatorController {
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static final WeaponsFactory weaponsFactory = WeaponsFactory.getInstance();
    private static final String NULL_JSON_STRING = "{}";
    private LobbyManager lobby;
    private BattlefieldModel bfModel;
    private SpectatorModel specModel;
    private boolean inited;

    public SpectatorController(LobbyManager lobby, BattlefieldModel bfModel, SpectatorModel specModel) {
        this.lobby = lobby;
        this.bfModel = bfModel;
        this.specModel = specModel;

    }

    public void executeCommand(Command cmd) {
        switch (cmd.type) {
            case BATTLE: {
                if (cmd.args[0].equals("spectator_user_init")) {
                    this.initUser();
                }
                if (cmd.args[0].equals("i_exit_from_battle")) {
                    this.lobby.onExitFromBattle();
                }
                if (!cmd.args[0].equals("chat")) break;
                this.specModel.getChatModel().onMessage(cmd.args[1], this);
                break;
            }
            default: {
                loggerService.log(LogType.INFO,
                        "[executeCommand(Command)::SpectatorController] : non-battle command " + cmd);
            }
        }
    }

    private void initUser() {
        try {
            this.inited = true;
            this.sendShotsData();
            if (this.bfModel.battleInfo.battleType.equals("CTF")) {
                this.sendCommand(Type.BATTLE, "init_ctf_model", JSONUtils.parseCTFModelData(this.bfModel));
            }
            if (this.bfModel.battleInfo.battleType.equals("DOM")) {
                this.bfModel.domModel.sendInitData(this);
            }
            this.sendCommand(Type.BATTLE, "init_gui_model", JSONUtils.parseBattleData(this.bfModel));
            this.sendCommand(Type.BATTLE, "init_inventory", NULL_JSON_STRING);
            this.bfModel.battleMinesModel.initModel(this);
            this.bfModel.battleMinesModel.sendMines(this);
            this.bfModel.sendAllTanks(this);
            if(this.bfModel.bonusesSpawnService != null) {
                this.bfModel.bonusesSpawnService.sendAlreadyDroppedBonuses(this);
                this.bfModel.bonusesSpawnService.displayAllDroppzones(this);
            }
            this.bfModel.effectsModel.sendInitData(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            this.lobby.kick();
        }
    }

    public String getId() {
        return this.lobby.getLocalUser().getNickname();
    }

    public User getUser() {
        return this.lobby.getLocalUser();
    }

    public void sendCommand(Type type, String... args) {
        if (this.inited) {
            this.lobby.send(type, args);
        }
    }

    private void sendShotsData() {
        this.sendCommand(Type.BATTLE, "init_shots_data", weaponsFactory.getJSONList());
        this.sendCommand(flashtanki.commands.Type.BATTLE, "init_sfx_data", weaponsFactory.getSFXData());

    }

    public void onDisconnect() {
        this.bfModel.spectatorModel.removeSpectator(this);
    }
}

