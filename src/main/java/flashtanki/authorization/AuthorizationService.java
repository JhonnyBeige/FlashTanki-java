/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.authorization;

import flashtanki.utils.RankUtils;
import flashtanki.captcha.CaptchaService;
import flashtanki.commands.Command;
import flashtanki.commands.Type;
import flashtanki.groups.UserGroupsLoader;
import flashtanki.json.JSONUtils;
import flashtanki.main.kafka.KafkaTemplateService;
import flashtanki.lobby.LobbyManager;
import flashtanki.lobby.chat.ChatLobby;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.main.netty.ProtocolTransfer;
import flashtanki.main.netty.blackip.model.BlackIPService;
import flashtanki.services.AutoEntryServices;
import flashtanki.system.SystemBattlesHandler;
import flashtanki.system.localization.Localization;
import flashtanki.users.User;
import flashtanki.users.friends.Friends;
import flashtanki.users.karma.Karma;

import java.util.regex.Pattern;

//import org.hibernate.annotations.common.util.impl.Log_.logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthorizationService {
    private static final DatabaseManager database = DatabaseManagerImpl.instance();
    private static final ChatLobby chatLobby = ChatLobby.getInstance();
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.getInstance();
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static final KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private static final CaptchaService captchaService = CaptchaService.getInstance();
    private static final BlackIPService blackIPService = BlackIPService.getInstance();
    private final static String GET_STARS_TOPIC = "get-stars-request";
    private final static String SYSTEM_MAIL_REQUEST_TOPIC = "send-system-mail-request";

    private static AuthorizationService instance;

    private AuthorizationService() {
    }

    public static AuthorizationService getInstance() {
        if (instance == null) {
            instance = new AuthorizationService();
        }
        return instance;
    }

    public void executeCommand(Command command, ProtocolTransfer protocolTransfer) {
        try {
            if (command.type == Type.AUTH) {
                if (command.args[0].equals("recovery_account")) {
                    String userEmail = command.args[1];
                    // check if email exists and is linked to any account
                    String nickname = database.getNicknameByEmail(userEmail);
                    boolean emailExists = nickname != null;
                    if (!emailExists) {
                        protocolTransfer.send(Type.AUTH, "recovery_account_result", "false");
                        return;
                    }
                    // save the username that the current session is allowed to reset the password
                    // of
                    protocolTransfer.setParam("restoringUser", nickname);
                    // process the email sending the code
                    User localUser = database.getUserByNickName(nickname);
                    localUser.setEmailConfirmationCode(String.valueOf((int) (Math.random() * 10000)));
                    database.update(localUser);
                    ObjectMapper objectMapper = new ObjectMapper();
                    //FIXME: no kafka
                    // kafkaTemplateService.getProducer().send(objectMapper.writeValueAsString(
                    //         java.util.Map.of("to", userEmail,
                    //                 "subject", "Email confirmation",
                    //                 "text", "Your confirmation code: " + localUser.getEmailConfirmationCode())),
                    //         SYSTEM_MAIL_REQUEST_TOPIC);
                    protocolTransfer.send(Type.AUTH, "recovery_account_code");
                    return;
                }
                if (command.args[0].equals("recovery_account_code")) {
                    User localUser = database.getUserByNickName(protocolTransfer.getParam("restoringUser"));
                    // check if code is equal to the code sent in email
                    String codeSentInEmail = localUser.getEmailConfirmationCode();
                    if (command.args[1].equals(codeSentInEmail)) {
                        protocolTransfer.send(Type.AUTH, "show_reset_password_form");
                    } else {
                        protocolTransfer.send(Type.AUTH, "recovery_account_result_code");
                    }
                    return;
                }
                if (command.args[0].equals("submit_reset_password")) {
                    String restoringUser = protocolTransfer.getParam("restoringUser");
                    User localUser = database.getUserByNickName(restoringUser);
                    String newPassword = command.args[1];
                    // reset user`s password
                    localUser.setPassword(newPassword);
                    database.update(localUser);

                    protocolTransfer.send(Type.AUTH, "recovery_account_done");
                    return;
                }
                if(protocolTransfer.getParam("auth") != null) {
                    protocolTransfer.closeConnection();
                    blackIPService.block(protocolTransfer.getIP());
                }
                String id = command.args[0];
                if (command.args.length <= 1) {
                    return;
                }
                String password = command.args[1];
                if (id.length() > 50) {
                    id = null;
                    return;
                }
                if (password.length() > 50) {
                    password = null;
                    return;
                }
                if (id.contains("@")) {
                    id = database.getNicknameByEmail(id);
                }
                User user = database.getUserByNickName(id);
                if (user == null) {
                    protocolTransfer.send(Type.AUTH, "not_exist");
                    return;
                }
                if (!user.getPassword().equals(password)) {
                    loggerService.log(LogType.INFO,
                            "The user " + user.getNickname() + " has not been logged. Password deined.");
                    protocolTransfer.send(Type.AUTH, "denied");
                    return;
                }
                protocolTransfer.setParam("auth", true);
                protocolTransfer.identify(user.getId());
                this.onPasswordAccept(user, protocolTransfer);
            } else if (command.type == Type.REGISTRATON) {
                if (command.args[0].equals("check_name")) {
                    String nickname = command.args[1];
                    if (nickname.length() > 50) {
                        nickname = null;
                        return;
                    }
                    boolean callsignExist = database.contains(nickname);
                    boolean callsignNormal = this.callsignNormal(nickname);
                    
                    loggerService.log(LogType.INFO, "callsign exist: " + nickname + " " + callsignExist);
                    protocolTransfer.send(Type.REGISTRATON, "check_name_result",
                            callsignExist || !callsignNormal ? "nickname_exist" : "not_exist");
                } else {
                    String nickname = command.args[0];
                    if (command.args.length < 4) {
                        return;
                    }
                    String password = command.args[1];
                    if (nickname.length() > 50 || password.length() > 50) {
                        return;
                    }

                    long captchaId = Long.parseLong(command.args[2]);
                    String captchaCode = command.args[3];
                    if (!captchaService.checkCaptcha(captchaCode, captchaId)) {
                        protocolTransfer.send(Type.REGISTRATON, "captcha_wrong");
                        return;
                    }

                    if (database.contains(nickname)) {
                        protocolTransfer.send(Type.REGISTRATON, "nickname_exist");
                        return;
                    }
                    if (this.callsignNormal(nickname)) {
                         User newUser = new User(nickname, password);
                         newUser.setLastIP("127.0.0.1"); //stub
                         database.register(newUser);
                         protocolTransfer.send(Type.REGISTRATON, "info_done");
                         this.createNewUser(newUser, protocolTransfer);
                         protocolTransfer.identify(newUser.getId());

                    } else {
                        protocolTransfer.closeConnection();
                    }
                }
            } else if (command.type == Type.SYSTEM) {
                String data = command.args[0];
                if (data.equals("init_location")) {
                    protocolTransfer.setParam("localization", Localization.valueOf(command.args[1]));
                }
                if (data.equals("c01")) {
                    protocolTransfer.closeConnection();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
        }
    }

    private boolean callsignNormal(String nick) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9][\\w.-]{2,21}");
        return pattern.matcher(nick).matches();
    }

    private void createNewUser(User user, ProtocolTransfer protocolTransfer) {
        try {
            Karma karma = database.getKarmaByUser(user);
            user.setKarma(karma);
            user.getAntiCheatData().ip = protocolTransfer.getIP();
            database.cache(user);
            user.setGarage(database.getGarageByUser(user));
            user.getGarage().unparseJSONData();
            user.setUserGroup(UserGroupsLoader.getUserGroup(user.getType()));
            loggerService.log(LogType.INFO, "User registered: " + user.getNickname() + " with ID: " + user.getId());
            protocolTransfer.lobby = new LobbyManager(protocolTransfer, user);
            if (protocolTransfer.getParam("localization") == null) {
                protocolTransfer.setParam("localization", Localization.EN);
            }

            user.setLocalization(protocolTransfer.getParam("localization"));
            protocolTransfer.send(Type.AUTH, "accept");
            protocolTransfer.send(Type.LOBBY, "init_panel", JSONUtils.parseUserToJSON(user));
            protocolTransfer.send(Type.LOBBY, "update_rang_progress",
                    String.valueOf(RankUtils.getUpdateNumber(user.getScore())));
            protocolTransfer.lobby.onEnterInBattle(SystemBattlesHandler.newbieBattleToEnter.battleId);
            if (protocolTransfer.lobby.battle == null) {
                protocolTransfer.send(Type.LOBBY, "init_battle_select", JSONUtils.parseBattleMapList());
                protocolTransfer.send(Type.LOBBY_CHAT, "init_chat");
                protocolTransfer.send(Type.LOBBY_CHAT, "init_messages",
                        JSONUtils.parseChatLobbyMessages(chatLobby.getMessages()));
            }
            user.setLastIP(user.getAntiCheatData().ip);
            database.update(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onPasswordAccept(User user, ProtocolTransfer protocolTransfer) {
        try {
            Karma karma = database.getKarmaByUser(user);
            user.setKarma(karma);
            Friends friendByUser = database.getFriendByUser(user);
            if (friendByUser != null) {
                String lastFriendReq = friendByUser.getIncoming();
                user.setLastFriendRequest(lastFriendReq);
            } else {
                user.setLastFriendRequest("");
            }
            if(karma != null){
                if (karma.isGameBlocked()) {
                    protocolTransfer.send(Type.AUTH, "ban", karma.getReasonGameBan());
                    return;
                }
            }

            user.getAntiCheatData().ip = protocolTransfer.getIP();
            user.setGarage(database.getGarageByUser(user));
            user.getGarage().unparseJSONData();
            user.setUserGroup(UserGroupsLoader.getUserGroup(user.getType()));
            loggerService.log(LogType.INFO, "The user " + user.getNickname() + " has been logged. Password accept.");
            protocolTransfer.lobby = new LobbyManager(protocolTransfer, user);
            database.cache(user);
            if (protocolTransfer.getParam("localization") == null) {
                protocolTransfer.setParam("localization", Localization.EN);
            }
            user.setLocalization(protocolTransfer.getParam("localization"));
            protocolTransfer.send(Type.AUTH, "accept");
            protocolTransfer.send(Type.LOBBY, "init_panel", JSONUtils.parseUserToJSON(user));

            // GetStarsRequest starsRequest = new GetStarsRequest(session.lobby.getLocalUser().getId());
            // String message = JSONUtils.parseConfiguratorEntity(starsRequest, GetStarsRequest.class);

            // KafkaTemplateService.getInstance().getProducer().send(message, GET_STARS_TOPIC);

            protocolTransfer.send(Type.LOBBY, "update_rang_progress",
                    String.valueOf(RankUtils.getUpdateNumber(user.getScore())));
            if (!autoEntryServices.needEnterToBattle(user)) {
                // session.send(Type.GARAGE, "init_garage_items",
                // JSONUtils.parseGarageUser(user).trim());
                // session.send(Type.GARAGE, "init_market",
                // JSONUtils.parseMarketItems(user));
                protocolTransfer.send(Type.LOBBY, "init_battle_select", JSONUtils.parseBattleMapList());
                protocolTransfer.send(Type.LOBBY_CHAT, "init_chat");
                protocolTransfer.send(Type.LOBBY_CHAT, "init_messages",
                        JSONUtils.parseChatLobbyMessages(chatLobby.getMessages()));
            } else {
                protocolTransfer.send(Type.LOBBY, "init_battlecontroller");
                autoEntryServices.prepareToEnter(protocolTransfer.lobby);
            }
            user.setLastIP(user.getAntiCheatData().ip);
            database.update(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
