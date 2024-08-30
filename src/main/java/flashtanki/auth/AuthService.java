/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.auth;

import flashtanki.utils.RankUtils;
import flashtanki.captcha.CaptchaService;
import flashtanki.commands.Command;
import flashtanki.commands.Type;
import flashtanki.groups.UserGroupsLoader;
import flashtanki.json.JSONUtils;
import flashtanki.kafka.KafkaTemplateService;
import flashtanki.lobby.LobbyManager;
import flashtanki.lobby.chat.ChatLobby;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.main.netty.Session;
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

public class AuthService {
    private static final DatabaseManager database = DatabaseManagerImpl.instance();
    private static final ChatLobby chatLobby = ChatLobby.getInstance();
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.getInstance();
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static final KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private static final CaptchaService captchaService = CaptchaService.getInstance();
    private static final BlackIPService blackIPService = BlackIPService.getInstance();
    private final static String GET_STARS_TOPIC = "get-stars-request";
    private final static String SYSTEM_MAIL_REQUEST_TOPIC = "send-system-mail-request";

    private static AuthService instance;

    private AuthService() {
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public void executeCommand(Command command, Session session) {
        try {
            if (command.type == Type.AUTH) {
                if (command.args[0].equals("recovery_account")) {
                    String userEmail = command.args[1];
                    // check if email exists and is linked to any account
                    String nickname = this.database.getNicknameByEmail(userEmail);
                    boolean emailExists = nickname != null;
                    if (!emailExists) {
                        session.send(Type.AUTH, "recovery_account_result", "false");
                        return;
                    }
                    // save the username that the current session is allowed to reset the password
                    // of
                    session.setParam("restoringUser", nickname);
                    // process the email sending the code
                    User localUser = this.database.getUserByNickName(nickname);
                    localUser.setEmailConfirmationCode(String.valueOf((int) (Math.random() * 10000)));
                    this.database.update(localUser);
                    ObjectMapper objectMapper = new ObjectMapper();
                    //FIXME: no kafka
                    // kafkaTemplateService.getProducer().send(objectMapper.writeValueAsString(
                    //         java.util.Map.of("to", userEmail,
                    //                 "subject", "Email confirmation",
                    //                 "text", "Your confirmation code: " + localUser.getEmailConfirmationCode())),
                    //         SYSTEM_MAIL_REQUEST_TOPIC);
                    session.send(Type.AUTH, "recovery_account_code");
                    return;
                }
                if (command.args[0].equals("recovery_account_code")) {
                    User localUser = this.database.getUserByNickName(session.getParam("restoringUser"));
                    // check if code is equal to the code sent in email
                    String codeSentInEmail = localUser.getEmailConfirmationCode();
                    if (command.args[1].equals(codeSentInEmail)) {
                        session.send(Type.AUTH, "show_reset_password_form");
                    } else {
                        session.send(Type.AUTH, "recovery_account_result_code");
                    }
                    return;
                }
                if (command.args[0].equals("submit_reset_password")) {
                    String restoringUser = session.getParam("restoringUser");
                    User localUser = database.getUserByNickName(restoringUser);
                    String newPassword = command.args[1];
                    // reset user`s password
                    localUser.setPassword(newPassword);
                    database.update(localUser);

                    session.send(Type.AUTH, "recovery_account_done");
                    return;
                }
                if(session.getParam("auth") != null) {
                    session.closeConnection();
                    blackIPService.block(session.getIP());
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
                    id = this.database.getNicknameByEmail(id);
                }
                User user = this.database.getUserByNickName(id);
                if (user == null) {
                    session.send(Type.AUTH, "not_exist");
                    return;
                }
                if (!user.getPassword().equals(password)) {
                    loggerService.log(LogType.INFO,
                            "The user " + user.getNickname() + " has not been logged. Password deined.");
                    session.send(Type.AUTH, "denied");
                    return;
                }
                session.setParam("auth", true);
                session.identify(user.getId());
                this.onPasswordAccept(user, session);
            } else if (command.type == Type.REGISTRATON) {
                if (command.args[0].equals("check_name")) {
                    String nickname = command.args[1];
                    if (nickname.length() > 50) {
                        nickname = null;
                        return;
                    }
                    boolean callsignExist = this.database.contains(nickname);
                    boolean callsignNormal = this.callsignNormal(nickname);
                    
                    loggerService.log(LogType.INFO, "callsign exist: " + nickname + " " + callsignExist);
                    session.send(Type.REGISTRATON, "check_name_result",
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
                        session.send(Type.REGISTRATON, "captcha_wrong");
                        return;
                    }

                    if (this.database.contains(nickname)) {
                        session.send(Type.REGISTRATON, "nickname_exist");
                        return;
                    }
                    if (this.callsignNormal(nickname)) {
                         User newUser = new User(nickname, password);
                         newUser.setLastIP("127.0.0.1"); //stub
                         this.database.register(newUser);
                         session.send(Type.REGISTRATON, "info_done");
                         this.createNewUser(newUser, session);
                         session.identify(newUser.getId());

                    } else {
                        session.closeConnection();
                    }
                }
            } else if (command.type == Type.SYSTEM) {
                String data = command.args[0];
                if (data.equals("init_location")) {
                    session.setParam("localization", Localization.valueOf(command.args[1]));
                }
                if (data.equals("c01")) {
                    session.closeConnection();
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

    private void createNewUser(User user, Session session) {
        try {
            Karma karma = this.database.getKarmaByUser(user);
            user.setKarma(karma);
            user.getAntiCheatData().ip = session.getIP();
            this.database.cache(user);
            user.setGarage(this.database.getGarageByUser(user));
            user.getGarage().unparseJSONData();
            user.setUserGroup(UserGroupsLoader.getUserGroup(user.getType()));
            loggerService.log(LogType.INFO, "User registered: " + user.getNickname() + " with ID: " + user.getId());
            session.lobby = new LobbyManager(session, user);
            if (session.getParam("localization") == null) {
                session.setParam("localization", Localization.EN);
            }

            user.setLocalization(session.getParam("localization"));
            session.send(Type.AUTH, "accept");
            session.send(Type.LOBBY, "init_panel", JSONUtils.parseUserToJSON(user));
            session.send(Type.LOBBY, "update_rang_progress",
                    String.valueOf(RankUtils.getUpdateNumber(user.getScore())));
            session.lobby.onEnterInBattle(SystemBattlesHandler.newbieBattleToEnter.battleId);
            if (session.lobby.battle == null) {
                session.send(Type.LOBBY, "init_battle_select", JSONUtils.parseBattleMapList());
                session.send(Type.LOBBY_CHAT, "init_chat");
                session.send(Type.LOBBY_CHAT, "init_messages",
                        JSONUtils.parseChatLobbyMessages(this.chatLobby.getMessages()));
            }
            user.setLastIP(user.getAntiCheatData().ip);
            this.database.update(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onPasswordAccept(User user, Session session) {
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
                    session.send(Type.AUTH, "ban", karma.getReasonGameBan());
                    return;
                }
            }

            user.getAntiCheatData().ip = session.getIP();
            user.setGarage(database.getGarageByUser(user));
            user.getGarage().unparseJSONData();
            user.setUserGroup(UserGroupsLoader.getUserGroup(user.getType()));
            loggerService.log(LogType.INFO, "The user " + user.getNickname() + " has been logged. Password accept.");
            session.lobby = new LobbyManager(session, user);
            database.cache(user);
            if (session.getParam("localization") == null) {
                session.setParam("localization", Localization.EN);
            }
            user.setLocalization(session.getParam("localization"));
            session.send(Type.AUTH, "accept");
            session.send(Type.LOBBY, "init_panel", JSONUtils.parseUserToJSON(user));

            // GetStarsRequest starsRequest = new GetStarsRequest(session.lobby.getLocalUser().getId());
            // String message = JSONUtils.parseConfiguratorEntity(starsRequest, GetStarsRequest.class);

            // KafkaTemplateService.getInstance().getProducer().send(message, GET_STARS_TOPIC);

            session.send(Type.LOBBY, "update_rang_progress",
                    String.valueOf(RankUtils.getUpdateNumber(user.getScore())));
            if (!autoEntryServices.needEnterToBattle(user)) {
                // session.send(Type.GARAGE, "init_garage_items",
                // JSONUtils.parseGarageUser(user).trim());
                // session.send(Type.GARAGE, "init_market",
                // JSONUtils.parseMarketItems(user));
                session.send(Type.LOBBY, "init_battle_select", JSONUtils.parseBattleMapList());
                session.send(Type.LOBBY_CHAT, "init_chat");
                session.send(Type.LOBBY_CHAT, "init_messages",
                        JSONUtils.parseChatLobbyMessages(this.chatLobby.getMessages()));
            } else {
                session.send(Type.LOBBY, "init_battlecontroller");
                this.autoEntryServices.prepareToEnter(session.lobby);
            }
            user.setLastIP(user.getAntiCheatData().ip);
            this.database.update(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
