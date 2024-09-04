/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.lobby;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flashtanki.utils.StringUtils;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.maps.Map;
import flashtanki.battles.maps.MapsLoaderService;
import flashtanki.battles.spectator.SpectatorController;
import flashtanki.commands.Command;
import flashtanki.commands.Type;
import flashtanki.users.garage.containers.ContainerSystem;
import flashtanki.json.JSONUtils;
import flashtanki.json.ShopFactory;
import flashtanki.main.kafka.KafkaTemplateService;
import flashtanki.main.kafka.extermalMessage.GetChallengeInfoRequest;
import flashtanki.lobby.battles.BattleInfo;
import flashtanki.lobby.battles.BattlesList;
import flashtanki.lobby.chat.ChatLobby;
import flashtanki.lobby.chat.ChatMessage;
import flashtanki.lobby.chat.flood.FloodController;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.main.netty.ProtocolTransfer;
import flashtanki.main.params.OnlineStats;
import flashtanki.users.premium.PremiumService;
import flashtanki.services.AutoEntryServices;
import flashtanki.services.GiftsServices;
import flashtanki.services.LobbysServices;
import flashtanki.battles.tanks.shoteffect.ShotEffectSystem;
import flashtanki.battles.tanks.skin.SkinSystem;
import flashtanki.system.missions.dailybonus.DailyBonusService;
import flashtanki.users.TypeUser;
import flashtanki.users.User;
import flashtanki.users.friends.FriendsService;
import flashtanki.users.friends.dto.FriendsResponse;
import flashtanki.users.garage.Garage;
import flashtanki.users.garage.GarageItemsLoader;
import flashtanki.users.garage.items.Item;
import flashtanki.users.locations.UserLocation;

import java.util.Date;

import java.io.IOException;
import java.util.Optional;

import lombok.SneakyThrows;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LobbyManager {
    private final User localUser;
    public ProtocolTransfer protocolTransfer;
    private final FloodController chatFloodController;

    public BattlefieldPlayerController battle;
    public SpectatorController spectatorController;
    private final static DatabaseManager database = DatabaseManagerImpl.instance();
    private final static FriendsService friendsService = FriendsService.getInstance();
    private final static BattlesList battlesList = BattlesList.getInstance();
    private final static SkinSystem skinSystem = SkinSystem.getInstance();
    private final static ShotEffectSystem shotEffectSystem = ShotEffectSystem.getInstance();
    private final static LobbysServices lobbysServices = LobbysServices.getInstance();
    private final static ChatLobby chatLobby = ChatLobby.getInstance();
    private final static DailyBonusService dailyBonusService = DailyBonusService.getInstance();
    private final static AutoEntryServices autoEntryServices = AutoEntryServices.getInstance();
    private final static KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private static final GiftsServices giftsService = GiftsServices.instance();
    private final static LoggerService loggerService = LoggerService.getInstance();
    private final static String CHALLENGE_INFO_REQUEST_TOPIC = "get-challenge-info-request";
    private final static String SYSTEM_MAIL_REQUEST_TOPIC = "send-system-mail-request";
    private final static String ADDED_BATTLE_SCORE_TOPIC = "added-battle-score-request";
    private final static PremiumService premiumService = PremiumService.getInstance();

    public LobbyManager(ProtocolTransfer protocolTransfer, User localUser) {
        this.protocolTransfer = protocolTransfer;
        this.localUser = localUser;
        this.chatFloodController = new FloodController();
        this.localUser.setUserLocation(UserLocation.BATTLESELECT);
        lobbysServices.addLobby(this);
        OnlineStats.addOnline(localUser.getNickname());
        dailyBonusService.userInited(this);
    }

    public void send(Type type, String... args) {
        try {
            this.protocolTransfer.send(type, args);
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public void executeCommand(Command cmd) {
        try {
            final Garage garage = this.localUser.getGarage();
            switch (cmd.type) {
                case LOBBY_CHAT: {
                    User userByNickName = database.getUserByNickName(cmd.args[2]);
                    System.out.println("user has prem: " + premiumService.getPremiumTime(this.localUser.getId()).isActivated());
                    chatLobby.addMessage(new ChatMessage(this.localUser, premiumService.getPremiumTime(this.localUser.getId()).isActivated(), cmd.args[0], this.stringToBoolean(cmd.args[1]),
                            cmd.args[2].equals("NULL") ? null : userByNickName, !cmd.args[2].equals("NULL") && premiumService.getPremiumTime(userByNickName.getId()).isActivated(), this));
                    break;
                }
                case GARAGE: {
                    execGarageCommand(cmd, garage);
                    break;
                }
                case LOBBY: {
                    execLobbyCommand(cmd);
                    break;
                }
                case BATTLE: {
                    if (this.battle != null) {
                        this.battle.executeCommand(cmd);
                    }
                    if (this.spectatorController != null) {
                        this.spectatorController.executeCommand(cmd);
                    }
                    break;
                }
                case SYSTEM: {
                    String data = cmd.args[0];
                    if (data.equals("c01")) {
                        this.kick();
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SneakyThrows
    private boolean execLobbyCommand(Command cmd) {
        if (cmd.args[0].equals("change_email")) {
            changeEmail(cmd);
        }
        // send_code_unbind_email
        if (cmd.args[0].equals("send_code_unbind_email")) {
            this.localUser.setEmailConfirmationCode(String.valueOf((int) (Math.random() * 10000)));
            database.update(this.localUser);
            ObjectMapper objectMapper = new ObjectMapper();
            //FIXME: no kafka
            // kafkaTemplateService.getProducer().send(objectMapper.writeValueAsString(
            //                 java.util.Map.of("to", this.localUser.getEmail(),
            //                         "subject", "Email unbind confirmation",
            //                         "text", "Your confirmation code: " + this.localUser.getEmailConfirmationCode())),
            //         SYSTEM_MAIL_REQUEST_TOPIC);
        }
        // unbind_email
        if (cmd.args[0].equals("unbind_email")) {
            if (cmd.args[1].equals(this.localUser.getEmailConfirmationCode())) {
                this.localUser.setEmail(null);
                this.localUser.setEmailConfirmed(false);
                this.localUser.setEmailConfirmationCode(null);
                database.update(this.localUser);
                this.send(Type.LOBBY, "email_unbinded");
            } else {
                this.send(Type.LOBBY, "wrong_email_unbinded_code");
            }
        }
        if (cmd.args[0].equals("send_confirm_email_code")) {
            this.localUser.setEmailConfirmationCode(String.valueOf((int) (Math.random() * 10000)));
            database.update(this.localUser);
            ObjectMapper objectMapper = new ObjectMapper();
            //FIXME: no kafka
            // kafkaTemplateService.getProducer().send(objectMapper.writeValueAsString(
            //                 java.util.Map.of("to", this.localUser.getEmail(),
            //                         "subject", "Email confirmation",
            //                         "text", "Your confirmation code: " + this.localUser.getEmailConfirmationCode())),
            //         SYSTEM_MAIL_REQUEST_TOPIC);
        }
        if (cmd.args.length > 0 && cmd.args[0].equals("confirm_email")) {
            if (cmd.args.length > 1 && cmd.args[1].equals(this.localUser.getEmailConfirmationCode())) {
                this.localUser.setEmailConfirmed(true);
                this.localUser.setEmailConfirmationCode(null);
                database.update(this.localUser);
                this.send(Type.LOBBY, "email_confirmed");
            } else {
                this.send(Type.LOBBY, "wrong_email_confirmation_code");
            }
        } else {
            this.send(Type.LOBBY, "invalid_command");
        }
        if (cmd.args[0].equals("get_challenges_info")) {
            this.send(Type.LOBBY, "init_challenges_panel" , JSONUtils.parseChallenges());
            //getChallengesInfo();
        }

        if (cmd.args[0].equals("get_garage_data")) {
            this.sendGarage();
        }
        if (cmd.args[0].equals("get_data_init_battle_select")) {
            this.sendMapsInit();
        }
        if (cmd.args[0].equals("check_battleName_for_forbidden_words")) {
            String _name = cmd.args.length > 0 ? cmd.args[1] : "";
            this.checkBattleName(_name);
        }
        if (cmd.args[0].equals("try_open_item")) {
            giftsService.userOnGiftsWindowOpen(this);
        }
        if (cmd.args[0].equals("try_roll_item")) {
            giftsService.tryRollItem(this);
        }
        if (cmd.args[0].equals("try_roll_items")) {
            giftsService.rollItems(this, Integer.parseInt(cmd.args[1]));
        }
        if (cmd.args[0].equals("try_create_battle_dm")) {
            tryCreateBattleDm(cmd);
        }
        if (cmd.args[0].equals("try_create_battle_tdm")) {
            this.tryCreateTDMBattle(cmd.args[1]);
        }
        if (cmd.args[0].equals("try_create_battle_ctf")) {
            this.tryCreateCTFBattle(cmd.args[1]);
        }
        if (cmd.args[0].equals("try_create_battle_dom")) {
            this.tryCreateDOMBattle(cmd.args[1]);
        }
        if (cmd.args[0].equals("get_show_battle_info")) {
            this.sendBattleInfo(cmd.args[1]);
        }
        if (cmd.args[0].equals("enter_battle")) {
            this.onEnterInBattle(cmd.args[1]);
        }
        if (cmd.args[0].equals("enter_battle_team")) {
            this.onEnterInTeamBattle(cmd.args[1], Boolean.parseBoolean(cmd.args[2]));
        }
        if (cmd.args[0].equals("enter_battle_spectator")) {
            if (this.getLocalUser().getType() == TypeUser.DEFAULT) {
                return true;
            }
            this.enterInBattleBySpectator(cmd.args[1]);
        }
        if (cmd.args[0].equals("user_inited")) {
            dailyBonusService.userLoaded(this);
        }
        if (cmd.args[0].equals("get_shop")) {
            this.send(Type.LOBBY, "open_shop", ShopFactory.getData());
        }
        if (cmd.args[0].equals("get_friends")) {
            getFriends();
        }
        if (cmd.args[0].equals("show_profile")) {
            this.send(Type.LOBBY, "show_profile", JSONUtils.parseShowProfile(this.localUser));
        }
        if (cmd.args[0].equals("show_quests")) {
            this.send(Type.LOBBY, "show_quests", JSONUtils.parseQuests());
        }
        if (cmd.args[0].equals("make_friend")) {
            this.makeFriend(cmd.args[1]);
        }
        if (cmd.args[0].equals("deny_friend")) {
            this.delIncomingFriend(cmd.args[1]);
        }
        if (cmd.args[0].equals("del_friend")) {
            this.delFriend(cmd.args[1]);
        }
        if (cmd.args[0].equals("accept_friend")) {
            this.acceptFriend(cmd.args[1]);
        }
        if (cmd.args[0].equals("change_password")) {
            this.changePassword(cmd.args[1], cmd.args[2]);
        }
        return false;
    }

    private void execGarageCommand(Command cmd, Garage garage) {
        if (cmd.args[0].equals("try_mount_item")) {
            garage.mountItem(cmd.args[1]);
        }
        if (cmd.args[0].equals("try_update_item")) {
            this.onTryUpdateItem(cmd.args[1]);
        }
        if (cmd.args[0].equals("get_garage_data") && this.localUser.getGarage().mountHull != null
                && this.localUser.getGarage().mountTurret != null
                && this.localUser.getGarage().mountColormap != null) {

            getGarageData(garage);
        }
        if (cmd.args[0].equals("try_buy_item")) {
            this.onTryBuyItem(cmd.args[1], Integer.parseInt(cmd.args[2]));
        }
        if (cmd.args[0].equals("buy_skin")) {
            skinSystem.buySkin(this.localUser.getId(), cmd.args[1]);
        }
        if (cmd.args[0].equals("buy_shot_effect")) {
            shotEffectSystem.buyShotEffect(this.localUser.getId(), cmd.args[1]);
        }
        if (cmd.args[0].equals("open_container_window")) {
            ContainerSystem.getInstance()
                    .openContainerWindow(this.localUser, cmd.args[1]);

            // TODO: start loading bar on the client
            this.send(Type.GARAGE, "open_container_window_accepted");
        }
        if (cmd.args[0].equals("get_containers")) {
            String userContainers = ContainerSystem.getInstance()
                    .getUserContainersResponse(this.localUser.getId());
            this.send(Type.GARAGE, "init_containers", userContainers);
        }
        if (cmd.args[0].equals("open_container")) {
            ContainerSystem.getInstance().openContainer(this.localUser, cmd.args[1]);
        }
        if (cmd.args[0].equals("equip_skin")) {
            skinSystem.equipSkin(this.localUser.getId(), cmd.args[1],
                    cmd.args.length <= 2 || Boolean.parseBoolean(cmd.args[2]));
        }
        if (cmd.args[0].equals("equip_shot_effect")) {
            shotEffectSystem.equipShotEffect(this.localUser.getId(), cmd.args[1],
                    cmd.args.length <= 2 || Boolean.parseBoolean(cmd.args[2]));
        }
        if (cmd.args[0].equals("get_skins_info_for_item")) {
            String skinsForItem = skinSystem.getSkinsForItem(cmd.args[1],
                    this.localUser.getId(),
                    this.localUser.getLocalization());
            this.send(Type.GARAGE, "init_skins_for_item", skinsForItem);
        }
        if (cmd.args[0].equals("unequip_skin")) {
            skinSystem.unmountAllSkinsByItem(this.localUser.getId(), cmd.args[1],
                    cmd.args.length <= 2 || Boolean.parseBoolean(cmd.args[2]));
        }
        if (cmd.args[0].equals("unequip_shot_effect")) {
            shotEffectSystem.unmountAllShotEffectsByItem(this.localUser.getId(), cmd.args[1],
                    cmd.args.length <= 2 || Boolean.parseBoolean(cmd.args[2]));
        }
        if (cmd.args[0].equals("get_shot_effects_info_for_item")) {
            this.send(Type.GARAGE, "init_shot_effects_for_item", shotEffectSystem
                    .getShotEffectsForItem(cmd.args[1],
                            this.localUser.getId(),
                            this.localUser.getLocalization()));
        }
        if (cmd.args[0].equals("try_microupgrade_item")) {
            tryMicroupgrade(cmd);
        }
    }

    private void getGarageData(Garage ga) {
        Optional<String> mountedHullSkin = SkinSystem.getInstance()
                .getMountedSkinForUserAndItem(ga.mountHull.id, this.localUser.getId());
        Optional<String> mountedTurretSkin = SkinSystem.getInstance()
                .getMountedSkinForUserAndItem(ga.mountTurret.id, this.localUser.getId());
        this.send(Type.GARAGE, "init_mounted_item",
                StringUtils.concatStrings(ga.mountHull.id, "_m",
                        String.valueOf(ga.mountHull.modificationIndex)),
                mountedHullSkin.orElse(StringUtils.concatStrings(ga.mountHull.id, "_m",
                        String.valueOf(ga.mountHull.modificationIndex))));
        this.send(Type.GARAGE, "init_mounted_item",
                StringUtils.concatStrings(ga.mountTurret.id, "_m",
                        String.valueOf(ga.mountTurret.modificationIndex)),
                mountedTurretSkin.orElse(StringUtils.concatStrings(ga.mountTurret.id, "_m",
                        String.valueOf(ga.mountTurret.modificationIndex))));
        this.send(Type.GARAGE, "init_mounted_item",
                StringUtils.concatStrings(this.localUser.getGarage().mountColormap.id, "_m",
                        String.valueOf(this.localUser.getGarage().mountColormap.modificationIndex)));
        if(this.localUser.getGarage().mountModule != null) {
            this.send(Type.GARAGE, "init_mounted_item",
                    StringUtils.concatStrings(this.localUser.getGarage().mountModule.id, "_m",
                            String.valueOf(this.localUser.getGarage().mountModule.modificationIndex)));
        }

    }

    private void tryMicroupgrade(Command cmd) {
        try {
            for (final Item item : this.localUser.getGarage().items) {
                if (item.getId().equals(cmd.args[1])) {
                    if (this.localUser.getCrystall() >= getUpgradePrice(item)) {
                        if (item.microUpgrades < 10) {
                            this.addCrystall(-(getUpgradePrice(item)));
                            item.microUpgrades++;
                            item.microUpgradePrice = getUpgradePrice(item);
                            this.localUser.getGarage().parseJSONData();
                            database.update(this.localUser.getGarage());
                            this.send(Type.GARAGE, "update_upgrade_info",
                                    String.valueOf(item.microUpgrades),
                                    String.valueOf(item.microUpgradePrice));
                        }
                    }
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void getFriends() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        FriendsResponse friendsByUser = friendsService.getFriendsByUser(this.localUser.getId());
        String json = objectMapper.writeValueAsString(friendsByUser);
        this.send(Type.LOBBY, "init_friends_list", json);
    }

    private void tryCreateBattleDm(Command cmd) {
        this.tryCreateBattleDM(cmd.args[1], cmd.args[2], Integer.parseInt(cmd.args[3]),
                Integer.parseInt(cmd.args[4]), Integer.parseInt(cmd.args[5]),
                Integer.parseInt(cmd.args[6]), Integer.parseInt(cmd.args[7]),
                this.stringToBoolean(cmd.args[8]), this.stringToBoolean(cmd.args[9]),
                this.stringToBoolean(cmd.args[10]), this.stringToBoolean(cmd.args[11]),
                this.stringToBoolean(cmd.args[12]),
                Integer.parseInt(cmd.args[13]));
    }

    private void getChallengesInfo() {
        GetChallengeInfoRequest challengeInfoRequest = new GetChallengeInfoRequest(
                this.localUser.getId());
        String message = JSONUtils.parseConfiguratorEntity(challengeInfoRequest,
                GetChallengeInfoRequest.class);
        try {
            //FIXME: no kafka
            // kafkaTemplateService.getProducer().send(message, CHALLENGE_INFO_REQUEST_TOPIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeEmail(Command cmd) {
        if (!this.localUser.isEmailConfirmed()) {
            this.localUser.setEmail(cmd.args[1]);
            this.localUser.setEmailConfirmed(false);
            database.update(this.localUser);
            this.send(Type.LOBBY, "mail_changed", cmd.args[1],
                    String.valueOf(this.localUser.isEmailConfirmed()));
        }
    }

    private void changePassword(String oldPass, String newPass) {

        if (!this.localUser.getPassword().equals(oldPass)) {
            this.send(Type.LOBBY, "wrong_old_password");
            return;
        }
        this.localUser.setPassword(newPass);
        database.update(this.localUser);
        this.send(Type.LOBBY, "password_changed");

    }

    private int getUpgradePrice(Item item) {
        int finalPrice = item.nextPrice;
        finalPrice *= 2.75;
        int[] upgradesGraph = {0, 3, 6, 8, 10, 12, 13, 14, 15, 20, 20};
        int progress = item.microUpgrades;
        if (progress == 0) {
            return 100;
        }
        int totalUpgradePercentage = upgradesGraph[progress];
        int value = Math.round((float) finalPrice * ((float) totalUpgradePercentage / 100));
        return value;
    }

    private void tryCreateDOMBattle(String json) {
        long currDate;
        long delta;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage(
                    "Your rank must be higher than private.");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage(
                        "You can create more battles after 5 minutes.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        JSONObject parser = null;
        try {
            parser = (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        BattleInfo battle = new BattleInfo();
        battle.battleType = "DOM";
        parseBattle(parser, battle);
        battle.numFlags = (int) ((Long) parser.get("numPointsScore")).longValue();
        battle.minRank = (int) ((Long) parser.get("minRang")).longValue();
        battle.maxRank = (int) ((Long) parser.get("maxRang")).longValue();
        battle.team = true;
        battle.time = (int) ((Long) parser.get("time")).longValue();
        battle.autobalance = (Boolean) parser.get("autoBalance");
        battle.battleCreator = this.localUser.getNickname();
        battle.battleFormat = (int) ((Long) parser.get("battleFormat")).longValue();
        if (battle.battleFormat != 0) {
            battle.equipmentChange = false;
        }
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }
        if (battle.maxPeople < 1) {
            battle.maxPeople = 1;
        }
        if (battle.time <= 0 && battle.numFlags <= 0) {
            battle.time = 15;
            battle.numFlags = 0;
        }
        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }
        if (battle.numKills > 999) {
            battle.numKills = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (map.minRank > battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 30) {
            return;
        }
        battlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void parseBattle(JSONObject parser, BattleInfo battle) {
        battle.isPaid = (Boolean) parser.get("pay");
        battle.microUpgrades = (Boolean) parser.get("microUpgrades");
        battle.equipmentChange = (Boolean) parser.get("equipmentChange");
        battle.withoutBonuses = (Boolean) parser.get("inventory");
        battle.inventory = !((Boolean) parser.get("inventory"));
        battle.isPrivate = (Boolean) parser.get("privateBattle");
        battle.friendlyFire = (Boolean) parser.get("frielndyFire");
        battle.name = (String) parser.get("gameName");
        battle.map = MapsLoaderService.maps.get((String) parser.get("mapId"));
        battle.maxPeople = (int) ((Long) parser.get("numPlayers")).longValue();
    }

    private void acceptFriend(String arg) {
        friendsService.acceptFriend(this.localUser.getId(), arg);
    }

    private void enterInBattleBySpectator(String battleId) {
        this.send(Type.LOBBY, "start_battle");
        BattleInfo battle = battlesList.getBattleInfoById(battleId);
        if (battle == null) {
            return;
        }
        this.spectatorController = new SpectatorController(this, battle.model, battle.model.spectatorModel);
        battle.model.spectatorModel.addSpectator(this.spectatorController);
        this.localUser.setUserLocation(UserLocation.BATTLE);
        this.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battle, true));
        loggerService.log(LogType.INFO, "User " + this.localUser.getNickname() + " enter in battle by spectator.");
    }

    private void sendTableMessage(String msg) {
        this.send(Type.LOBBY, "server_message", msg);
    }

    private void tryCreateCTFBattle(String json) {
        Date banTo;
        long currDate;
        long delta;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage(
                    "Your rank must be higher than private");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage(
                        "You can create more battles after 5 minutes.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        JSONObject parser = null;
        try {
            parser = (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BattleInfo battle = new BattleInfo();
        battle.battleType = "CTF";
        parseBattle(parser, battle);
        battle.numFlags = (int) ((Long) parser.get("numFlags")).longValue();
        battle.minRank = (int) ((Long) parser.get("minRang")).longValue();
        battle.maxRank = (int) ((Long) parser.get("maxRang")).longValue();
        battle.team = true;
        battle.time = (int) ((Long) parser.get("time")).longValue();
        battle.autobalance = (Boolean) parser.get("autoBalance");
        battle.battleCreator = this.localUser.getNickname();
        battle.battleFormat = (int) ((Long) parser.get("battleFormat")).longValue();
        if (battle.battleFormat != 0) {
            battle.equipmentChange = false;
        }
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }
        if (battle.maxPeople < 1) {
            battle.maxPeople = 1;
        }
        if (battle.time <= 0 && battle.numFlags <= 0) {
            battle.time = 15;
            battle.numFlags = 0;
        }
        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }
        if (battle.numFlags > 999) {
            battle.numFlags = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (map.minRank > battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 30) {
            return;
        }
        battlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void tryCreateTDMBattle(String json) {
        Date banTo;
        long currDate;
        long delta;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage(
                    "Your rank must be higher than private.");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage(
                        "You can create more battles after 5 minutes.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        JSONObject parser = null;
        try {
            parser = (JSONObject) new JSONParser().parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BattleInfo battle = new BattleInfo();
        battle.battleType = "TDM";
        parseBattle(parser, battle);
        battle.numKills = (int) ((Long) parser.get("numKills")).longValue();
        battle.minRank = (int) ((Long) parser.get("minRang")).longValue();
        battle.maxRank = (int) ((Long) parser.get("maxRang")).longValue();
        battle.team = true;
        battle.time = (int) ((Long) parser.get("time")).longValue();
        battle.autobalance = (Boolean) parser.get("autoBalance");
        battle.battleCreator = this.localUser.getNickname();
        battle.battleFormat = (int) ((Long) parser.get("battleFormat")).longValue();
        if (battle.battleFormat != 0) {
            battle.equipmentChange = false;
        }
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }
        if (battle.maxPeople < 1) {
            battle.maxPeople = 1;
        }
        if (battle.time <= 0 && battle.numKills <= 0) {
            battle.time = 900;
            battle.numKills = 0;
        }
        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }
        if (battle.numKills > 999) {
            battle.numKills = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 30) {
            return;
        }
        if (map.minRank > battle.minRank) {
            return;
        }
        battlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void delIncomingFriend(final String uid1) {
        friendsService.danyFriend(this.localUser.getId(), uid1);
    }

    private void delFriend(final String uid1) {
        friendsService.delFriend(this.localUser.getId(), uid1);
    }

    private void makeFriend(final String nickname) {
        friendsService.addFriend(this.localUser.getId(), nickname);
    }

    public void onExitFromBattle() {
        if (this.battle != null) {
            long battleScore = this.battle.battle.players.get(this.localUser.getNickname()).statistic.getScore();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String message = objectMapper.writeValueAsString(java.util.Map.of(
                        "userId", this.localUser.getId(),
                        "score", battleScore));
                if(battleScore<=0){
                    RemoteDatabaseLogger.error( new Exception("Score value must be greater than 0"));
                }
                //FIXME: no kafka
                //kafkaTemplateService.getProducer().send(message, ADDED_BATTLE_SCORE_TOPIC);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            this.battle.destroy(autoEntryServices.removePlayer(this.battle.battle, this.getLocalUser().getNickname(),
                    this.battle.playerTeamType, this.battle.battle.battleInfo.team));
            this.battle = null;
        }
        if (this.spectatorController != null) {
            this.spectatorController.onDisconnect();
            this.spectatorController = null;
        }
        this.sendChatInit();
        this.sendMapsInit();
    }

    public void sendChatInit() {
        this.send(Type.LOBBY_CHAT, "init_messages", JSONUtils.parseChatLobbyMessages(chatLobby.getMessages()));
    }

    public void onExitFromBattleToGarage() {
        if (this.battle != null) {
            this.battle.destroy(autoEntryServices.removePlayer(this.battle.battle, this.getLocalUser().getNickname(),
                    this.battle.playerTeamType, this.battle.battle.battleInfo.team));
            this.battle = null;
        }
        if (this.spectatorController != null) {
            this.spectatorController.onDisconnect();
            this.spectatorController = null;
        }
        this.sendChatInit();
        this.sendGarage();
    }

    public void onExitFromStatistic() {
        this.onExitFromBattle();
        this.sendMapsInit();
    }

    private void onEnterInTeamBattle(String battleId, boolean red) {
        BattleInfo battleInfo = battlesList.getBattleInfoById(battleId);
        if (battleInfo.battleFormat != 0) {
            switch (battleInfo.battleFormat) {
                case 1:
                    if ((this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("hornet")
                            || this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("wasp")) &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("railgun")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
                case 2:
                    if (this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("hornet") &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("railgun")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
                case 3:
                    if (this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("wasp") &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("railgun")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
                case 4:
                    if (this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("wasp") &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("thunder")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
            }
        }
        this.send(Type.LOBBY, "start_battle");
        this.localUser.setUserLocation(UserLocation.BATTLE);
        if (this.battle != null) {
            return;
        }
        if (battleInfo == null) {
            return;
        }
        if (battleInfo.model.players.size() >= battleInfo.maxPeople * 2) {
            return;
        }
        if (red) {
            ++battleInfo.redPeople;
        } else {
            ++battleInfo.bluePeople;
        }
        this.battle = new BattlefieldPlayerController(this, battleInfo.model, red ? "RED" : "BLUE");
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle",
                JSONUtils.parseUpdateCoundPeoplesCommand(battleInfo));
        this.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battleInfo, false));
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "add_player_to_battle",
                JSONUtils.parseAddPlayerComand(this.battle, battleInfo));
    }

    public void onEnterInBattle(String battleId) {
        autoEntryServices.removePlayer(this.getLocalUser().getNickname());
        if (this.battle != null) {
            return;
        }
        BattleInfo battleInfo = battlesList.getBattleInfoById(battleId);
        if (battleInfo == null) {
            return;
        }
        if (battleInfo.model.players.size() >= battleInfo.maxPeople) {
            return;
        }
        if (battleInfo.battleFormat != 0) {
            switch (battleInfo.battleFormat) {
                case 1:
                    if ((this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("hornet")
                            || this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("wasp")) &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("railgun")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
                case 2:
                    if (this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("hornet") &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("railgun")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
                case 3:
                    if (this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("wasp") &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("railgun")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
                case 4:
                    if (this.getLocalUser().getGarage().mountHull.id.equalsIgnoreCase("wasp") &&
                            this.getLocalUser().getGarage().mountTurret.id.equalsIgnoreCase("thunder")) {
                        break;
                    }
                    this.send(Type.LOBBY, "error_enter_equipment");
                    return;
            }
        }
        this.send(Type.LOBBY, "start_battle");
        this.localUser.setUserLocation(UserLocation.BATTLE);
        this.battle = new BattlefieldPlayerController(this, battleInfo.model, "NONE");
        ++battleInfo.countPeople;
        if (!battleInfo.team) {
            lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT,
                    StringUtils.concatStrings("update_count_users_in_dm_battle", ";", battleId, ";",
                            String.valueOf(this.battle.battle.battleInfo.countPeople)));
        } else {
            lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT,
                    "update_count_users_in_team_battle", JSONUtils.parseUpdateCoundPeoplesCommand(battleInfo));
        }
        this.send(Type.BATTLE, "init_battle_model", JSONUtils.parseBattleModelInfo(battleInfo, false));
        lobbysServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "add_player_to_battle",
                JSONUtils.parseAddPlayerComand(this.battle, battleInfo));
    }

    private void sendBattleInfo(String id) {
        this.send(Type.LOBBY, "show_battle_info", JSONUtils.parseBattleInfoShow(battlesList.getBattleInfoById(id),
                this.getLocalUser().getType() != TypeUser.DEFAULT && this.getLocalUser().getType() != TypeUser.CHATMODERATOR && this.getLocalUser().getType() != TypeUser.CHATMODERATORCANDIDATE && this.getLocalUser().getType() != TypeUser.TESTER));
    }

    private void tryCreateBattleDM(String gameName, String mapId, int time, int kills, int maxPlayers, int minRang,
                                   int maxRang, boolean isPrivate, boolean pay, boolean noBonuses, boolean microUpgrades,
                                   boolean equipmentChange, int battleFormat) {
        Date banTo;
        long currDate;
        long delta;
        if (this.localUser.getRang() < 1) {
            this.sendTableMessage(
                    "Your rank must be higher than private");
            ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
            return;
        }
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }
                this.sendTableMessage(
                        "You can create more battles after 5 minutes.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }
        BattleInfo battle = new BattleInfo();
        Map map = MapsLoaderService.maps.get(mapId);
        if (maxRang < minRang) {
            maxRang = minRang;
        }
        if (maxPlayers < 2) {
            maxPlayers = 2;
        }
        if (time <= 0 && kills <= 0) {
            time = 900;
            kills = 0;
        }
        if (maxPlayers > map.maxPlayers) {
            maxPlayers = map.maxPlayers;
        }
        if (kills > 999) {
            kills = 999;
        }
        if (this.localUser.getRang() + 1 < battle.minRank) {
            return;
        }
        if (battle.time > 59940) {
            battle.time = 59940;
        }
        if (battle.maxRank > 30) {
            return;
        }
        if (map.minRank > minRang) {
            return;
        }
        battle.name = gameName;
        battle.map = MapsLoaderService.maps.get(mapId);
        battle.time = time;
        battle.numKills = kills;
        battle.maxPeople = maxPlayers;
        battle.minRank = minRang;
        battle.countPeople = 0;
        battle.maxRank = maxRang;
        battle.team = false;
        battle.isPrivate = isPrivate;
        battle.isPaid = pay;
        battle.microUpgrades = microUpgrades;
        battle.equipmentChange = equipmentChange;
        battle.withoutBonuses = noBonuses;
        battle.inventory = !noBonuses;
        battle.battleCreator = this.localUser.getNickname();
        battle.battleFormat = battleFormat;
        if (battleFormat != 0) {
            battle.equipmentChange = false;
        }
        battlesList.tryCreateBatle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void checkBattleName(String name) {
        this.send(Type.LOBBY, "check_battle_name", name);
    }

    public void sendMapsInit() {
        this.localUser.setUserLocation(UserLocation.BATTLESELECT);
        this.send(Type.LOBBY, "init_battle_select", JSONUtils.parseBattleMapList());
    }

    private void sendGarage() {
        if (this.battle != null) {
            this.localUser.setUserLocation(UserLocation.ALL);
        } else {
            this.localUser.setUserLocation(UserLocation.GARAGE);
        }
        this.send(Type.GARAGE, "init_market", JSONUtils.parseMarketItems(this.localUser));
        this.send(Type.GARAGE, "init_garage_items", JSONUtils.parseGarageUser(this.localUser).trim());
    }

    public synchronized void onTryUpdateItem(String id) {
        String itemId = id.substring(0, id.length() - 3);
        int modificationID = Integer.parseInt(id.substring(id.length() - 1)) + 1;
        Item item = GarageItemsLoader.getInstance().items.get(itemId);

        if (this.checkMoney(item.modifications[modificationID].price)) {
            if (this.getLocalUser().getRang() + 1 < item.modifications[modificationID].rank) {
                return;
            }
            this.localUser.getGarage().giveItem(itemId + "_m" + modificationID, 1,
                    () -> this.addCrystall(-item.modifications[modificationID].price),
                    () -> {
                    });

        }
    }

    public synchronized void onTryBuyItem(String itemId, int count) {
        if (count <= 0 || count > 9999) {
            this.crystallToZero();
            return;
        }
        Item item = GarageItemsLoader.getInstance().items.get(itemId.substring(0, itemId.length() - 3));
        int price = item.price * count;
        int itemRang = item.modifications[0].rank;
        if (this.checkMoney(price)) {
            if (this.getLocalUser().getRang() + 1 < itemRang) {
                return;
            }
            this.localUser.getGarage().giveItem(itemId, count,
                    () -> {
                        this.addCrystall(-price);
                    },
                    () -> {
                    });
        }
    }

    private boolean checkMoney(int buyValue) {
        return this.localUser.getCrystall() - buyValue >= 0;
    }

    public synchronized void addCrystall(int value) {
        this.localUser.addCrystall(value);
        this.send(Type.LOBBY, "add_crystall", String.valueOf(this.localUser.getCrystall()));
        database.update(this.localUser);
    }

    public void crystallToZero() {
        this.localUser.setCrystall(0);
        this.send(Type.LOBBY, "add_crystall", String.valueOf(this.localUser.getCrystall()));
        database.update(this.localUser);
    }

    private boolean stringToBoolean(String src) {
        return src.equalsIgnoreCase("true");
    }

    public void onDisconnect() {
        database.uncache(this.localUser.getNickname());
        lobbysServices.removeLobby(this);
        OnlineStats.removeInOnline(this.localUser.getNickname());
        if (this.spectatorController != null) {
            this.spectatorController.onDisconnect();
            this.spectatorController = null;
        }
        if (this.battle != null) {
            this.battle.onDisconnect();
            this.battle = null;
        }
    }

    public void kick() {
        this.protocolTransfer.closeConnection();
    }

    public User getLocalUser() {
        return this.localUser;
    }

    public FloodController getChatFloodController() {
        return this.chatFloodController;
    }

}
