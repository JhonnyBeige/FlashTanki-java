/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.lobby.chat;

import flashtanki.utils.StringUtils;
import flashtanki.commands.Type;
import flashtanki.groups.UserGroupsLoader;
import flashtanki.json.JSONUtils;
import flashtanki.lobby.LobbyManager;
import flashtanki.lobby.battles.BattleInfo;
import flashtanki.lobby.battles.BattlesList;
import flashtanki.lobby.shop.GiveItemService;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.main.netty.NettyUsersHandlerController;
import flashtanki.main.netty.blackip.model.BlackIPService;
import flashtanki.main.params.OnlineStats;
import flashtanki.services.AutoEntryServices;
import flashtanki.services.BanServices;
import flashtanki.services.LobbysServices;
import flashtanki.services.TanksServices;
import flashtanki.services.ban.BanChatCommads;
import flashtanki.services.ban.BanTimeType;
import flashtanki.services.ban.BanType;
import flashtanki.services.ban.DateFormater;
import flashtanki.services.ban.block.BlockGameReason;
import flashtanki.system.timers.SystemTimerScheduler;
import flashtanki.users.TypeUser;
import flashtanki.users.User;
import flashtanki.users.karma.Karma;
import flashtanki.users.locations.UserLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class ChatLobby {
    private static ChatLobby instance;
    private final ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    private boolean stopped = false;
    private final TanksServices tanksServices = TanksServices.getInstance();
    private final LobbysServices lobbyServices = LobbysServices.getInstance();
    private final DatabaseManager database = DatabaseManagerImpl.instance();
    private final BanServices banServices = BanServices.getInstance();
    private final BlackIPService blackIPService = BlackIPService.getInstance();
    private final AutoEntryServices autoEntryServices = AutoEntryServices.getInstance();
    private final BattlesList battlesList = BattlesList.getInstance();
    private final LoggerService loggerService = LoggerService.getInstance();

    public static ChatLobby getInstance() {
        if (instance == null) {
            instance = new ChatLobby();
        }
        return instance;
    }

    public void addMessage(ChatMessage msg) {
        this.checkSyntax(msg);
    }

    public void checkSyntax(ChatMessage msg) {
        block95:
        {
            block93:
            {
                Date banTo;
                long currDate;
                long delta;
                msg.message = msg.message.trim();

                Karma karma = this.database.getKarmaByUser(msg.user);
                if (karma.isChatBanned() && (delta = System.currentTimeMillis()
                        - (banTo = karma.getChatBannedBefore()).getTime()) <= 0L) {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", StringUtils.concatStrings(
                            "You are disconnected from the chat. You will be unmuted in ",
                            DateFormater.formatTimeToUnban(delta),
                            ". Reason: " + karma.getReasonChatBan()));
                    return;
                }
                if (!msg.message.startsWith("/"))
                    break block93;
                String temp = msg.message.replace('/', ' ').trim();
                String[] arguments = temp.split(" ");
                System.out.println(msg.user.getUserGroup().toString());
                if (!msg.user.getUserGroup().isAvaliableChatCommand(arguments[0])) {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "Unknown command");
                    return;
                }
                if (msg.user.getType() == TypeUser.DEFAULT) {
                    System.out.println("user default");
                    return;
                }
                switch (arguments[0]) {
                    case "system": {
                        system(arguments);
                        break;
                    }
                    case "warn": {
                        this.sendSystemMessageToAll(arguments, true);
                        break;
                    }
                    case "clear": {
                        this.clear();
                        break;
                    }
                    case "stop": {
                        stop(true, "Chat has stopped");
                        break;
                    }
                    case "start": {
                        stop(false, "Chat has started");
                        break;
                    }
                    case "addcry": {
                        addCry(msg, arguments);
                        break;
                    }
                    case "addscore": {
                        addScore(msg, arguments);
                        break;
                    }
                    case "kick": {
                        kick(msg, arguments);
                        break;
                    }
                    case "online": {
                        online(msg);
                        break;
                    }
                    case "karma": {
                        karmka(msg, arguments);
                        break;
                    }
                    case "rbattle": {
                        rbattle(msg, arguments);
                        break;
                    }
                    case "getip": {
                        getIp(msg, arguments);
                        break;
                    }
                    case "banip": {
                        banIp(msg, arguments);
                        break;
                    }
                    case "unbanip": {
                        unbanIp(msg, arguments);
                        break;
                    }
                    case "clean": {
                        clean(arguments);
                        break;
                    }
                    case "cleant": {
                        cleant(arguments);
                        break;
                    }
                    case "unban": {
                        unban(msg, arguments);
                        break;
                    }
                    case "w": {
                        warn(msg, arguments);
                        break;
                    }
                    case "blockgame": {
                        blockGame(msg, arguments);
                        break;
                    }
                    case "unblockgame": {
                        unblockGame(msg, arguments);
                        break;
                    }
                    case "finishAllBattles": {
                        AtomicInteger i = new AtomicInteger();
                        BattlesList.getInstance().getList().forEach(battleInfo -> {
                            battleInfo.model.chatModel.sendSystemMessage("The battle has been forcibly terminated");
                            battleInfo.model.tanksKillModel.restartBattle(false);
                            i.getAndIncrement();
                        });
                        msg.localLobby.send(Type.LOBBY_CHAT, "system",
                                "[SERVER]: All battles have been forcibly terminated. Total: " + i.get());
                        break;
                    }
                    case "permission":{
                        premission(msg, arguments);
                        break;

                    }
                    case "ddosLog": {
                        String s = NettyUsersHandlerController.getInstance()
                                .ddosLog();
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", s);
                       loggerService.log(LogType.INFO, s);
                        break;
                    }
                    case "giveitem": {
                        if(arguments.length < 4){
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "Not enough arguments");
                        }else{
                            User giveToUser = this.database.getUserByNickName(arguments[1]);
                            GiveItemService giveItemService = GiveItemService.getInstance();
                            long giveUserId = giveToUser.getId();
                            String giveItemId = arguments[2];
                            int giveItemCount = this.getInt(arguments[3]) * (giveItemId.contains("prem") ? 86000 : 1);
                            String jsonRequest = "{\"userId\":"+giveUserId+",\"itemId\":\""+giveItemId+"\",\"count\":"+giveItemCount+"}";
                            giveItemService.onReceive(jsonRequest);

                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "Given to: " + giveToUser.getNickname() + " " + giveUserId + " Item: " + giveItemId + " count:" + giveItemCount);
                            loggerService.log(LogType.INFO, "Given to: " + giveToUser.getNickname() + " " + giveUserId + " Item: " + giveItemId + " count:" + giveItemCount);
                        }
                        break;
                    }
                    default: {
                        if (msg.message.startsWith("/ban"))
                            break;
                        msg.localLobby.send(Type.LOBBY_CHAT, "system",
                                "[SERVER]: Unknown command!");
                    }
                }

                if (msg.message.startsWith("/ban")) {
                    ban(msg, arguments);
                }
                break block95;
            }
            if (!msg.message.isEmpty()) {
                if (msg.message.length() >= 399) {
                    msg = null;
                    return;
                }
                if (!this.stopped) {
                    if (!msg.localLobby.getChatFloodController().detected(msg.message)) {
                        msg.message = this.getNormalMessage(msg.message.trim());
                        if (this.chatMessages.size() >= 50) {
                            this.chatMessages.remove(0);
                        }
                        this.chatMessages.add(msg);
                        this.sendMessageToAll(msg);
                    } else {
                        if (msg.user.getWarnings() >= 4) {
                            BanTimeType time = BanTimeType.FIVE_MINUTES;
                            String reason = "Flood.";
                            this.banServices.ban(BanType.CHAT, time, msg.user, msg.user, reason);
                            this.sendSystemMessageToAll(
                                    StringUtils.concatStrings("Tanker ", msg.user.getNickname(),
                                            " has been muted for ", time.getNameType(), " Reason: ", reason),
                                    false);
                            return;
                        }
                        this.sendSystemMessageToAll("Tanker "
                                        + msg.user.getNickname()
                                        + "  has been warned Reason: Flood.",
                                false);
                        msg.user.addWarning();
                    }
                }
            }
        }
    }

    private void ban(ChatMessage msg, String[] arguments) {
        BanTimeType time = BanChatCommads.getTimeType(arguments[0]);
        if (arguments.length < 3) {
            return;
        }
        String reason = StringUtils.concatMassive(arguments, 2);
        if (time == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: Ban command not found!");
            return;
        }
        User _victim = this.database.getUserByNickName(arguments[1]);
        if (_victim == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: Ban command not found!");
            return;
        }
        this.banServices.ban(BanType.CHAT, time, _victim, msg.user, reason);
        this.sendSystemMessageToAll(StringUtils.concatStrings("Tanker ",
                _victim.getNickname(),
                " has been muted for the chat ",
                time.getNameType(), " Reason: ", reason), false);
    }

    private void premission(ChatMessage msg, String[] arguments) {
        if (arguments.length < 3) {
            return;
        }
        User user = this.database.getUserByNickName(arguments[1]);
        if (user == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: User not found!");
            return;
        }
        user.setType(TypeUser.values()[Integer.parseInt(arguments[2])]);
        user.setUserGroup(UserGroupsLoader.getUserGroup(user.getType()));
        database.update(user);
        msg.localLobby.send(Type.LOBBY_CHAT, "system",
                "[SERVER]: User " + user.getNickname() + " has been given the " + user.getType().toString() + " rights");
    }

    private void unblockGame(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2) {
            return;
        }
        User av = this.database.getUserByNickName(arguments[1]);
        if (av == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: User not found!");
            return;
        }
        this.banServices.unblock(av);
        this.sendSystemMessageToAll(StringUtils.concatStrings(
                        "Tanker ", av.getNickname(),
                        " was unlocked"),
                false);
    }

    private void blockGame(ChatMessage msg, String[] arguments) {
        if (arguments.length < 3) {
            return;
        }
        User victim_ = this.database.getUserByNickName(arguments[1]);
        try {
            if (victim_ == null ) {
                victim_ = this.database.getUserById(Long.valueOf(arguments[1]));
            }
        }catch (Exception ex) {
            victim_ = null;
        }
        int reasonId = 0;
        try {
            reasonId = Integer.parseInt(arguments[2]);
        } catch (Exception ex) {
            reasonId = 0;
        }
        if (victim_ == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: User not found!");
            return;
        }

        BanTimeType time = BanTimeType.FOREVER;
        if (arguments.length >= 4) {
            time = BanChatCommads.getTimeType(arguments[3]);
        }

        this.banServices.ban(BanType.GAME, time, victim_, msg.user,
                BlockGameReason.getReasonById(reasonId).getReason());
        LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
        if (lobby != null) {
            lobby.kick();
        }
        this.sendSystemMessageToAll(StringUtils.concatStrings(
                        "Tanker ", victim_.getNickname(),
                        " was blocked and kicked"),
                false);
    }

    private void warn(ChatMessage msg, String[] arguments) {
        if (arguments.length < 3) {
            return;
        }
        User giver = this.database.getUserByNickName(arguments[1]);
        if (giver == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: User not found!");
            return;
        }
        String reason = StringUtils.concatMassive(arguments, 2);
        this.sendSystemMessageToAll(StringUtils.concatStrings(
                "Tanker ", giver.getNickname(),
                " has been warned Cause: ",
                reason), false);
    }

    private void unban(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2)
            return;
        User cu = this.database.getUserByNickName(arguments[1]);
        if (cu == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: Player not found!");
            return;
        }
        this.banServices.unbanChat(cu);
        this.sendSystemMessageToAll(StringUtils.concatStrings(
                        "Tanker ", cu.getNickname(),
                        " is now unmuted"),
                false);
    }

    private void cleant(String[] arguments) {
        if (arguments.length < 2)
            return;
        this.cleanMessagesByText(StringUtils.concatMassive(arguments, 1));
    }

    private void clean(String[] arguments) {
        if (arguments.length < 2)
            return;
        this.cleanMessagesByUser(arguments[1]);
    }

    private void unbanIp(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2) {
            return;
        }
        User _victim = this.database.getUserByNickName(arguments[1]);
        if (_victim == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: User not found");
            return;
        }
        LobbyManager _l = this.lobbyServices.getLobbyByUser(_victim);
        blackIPService.unblock(_l.getLocalUser().getLastIP());
    }

    private void banIp(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2)
            return;
        User victim = this.database.getUserByNickName(arguments[1]);
        if (victim == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: User not found");
            return;
        }
        LobbyManager l = this.lobbyServices.getLobbyByUser(victim);
        blackIPService.block(l.getLocalUser().getLastIP());
        l.kick();
    }

    private void getIp(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2)
            return;
        User shower = this.database.getUserByNickName(arguments[1]);
        if (shower == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: The user was not found");
            return;
        }
        String ip = shower.getAntiCheatData().ip;
        if (ip == null) {
            ip = shower.getLastIP();
        }
        msg.localLobby.send(Type.LOBBY_CHAT, "system", "IP user " + shower.getNickname() + " : " + ip);
    }

    private void rbattle(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2)
            return;
        StringBuilder id = new StringBuilder();
        for (int i = 1; i < arguments.length; ++i) {
            id.append(arguments[i]).append(" ");
        }
        BattleInfo battle = battlesList.getBattleInfoById(id.toString().trim().replace("#battle", ""));
        if (battle == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: Battle not found");
            return;
        }
        if (battle.model != null) {
            battle.model.sendTableMessageToPlayers(
                    "The battle has ended early, you will be kicked soon");
        }
        SystemTimerScheduler.scheduleTask(() -> {
            this.sendSystemMessageToAll("Battle " + battle.name
                            + " was forcibly terminated",
                    false);
            battlesList.removeBattle(battle);
            autoEntryServices.battleDisposed(battle.model);
        }, 4000L);
        msg.localLobby.send(Type.LOBBY_CHAT, "system",
                "[SERVER]: The battle will be deleted after 4 seconds");
    }

    private void karmka(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    StringUtils.concatStrings("[KARMA]:\n", "Not enough arguments given"));
        }else {
            String karmaInfo = this.database.getKarmaByNickname(arguments[1]).toString();
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    StringUtils.concatStrings("[KARMA]:\n", karmaInfo));
        }
    }

    private static void online(ChatMessage msg) {
        msg.localLobby.send(Type.LOBBY_CHAT, "system", "Current online: " + OnlineStats.getOnline()
                + "\nMax online: " + OnlineStats.getMaxOnline());
    }

    private void kick(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2)
            return;
        User _userForKick = this.database.getUserByNickName(arguments[1]);
        if (_userForKick == null) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: Player not found");
            return;
        }
        LobbyManager _lobby = this.lobbyServices.getLobbyByUser(_userForKick);
        if (_lobby == null)
            return;
        _lobby.kick();
        this.sendSystemMessageToAll(
                _userForKick.getNickname() + " was kicked",
                false);
    }

    private void addScore(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2)
            return;
        int score = this.getInt(arguments[1]);
        if (msg.localLobby.getLocalUser().getScore() + score < 0) {
            msg.localLobby.send(Type.LOBBY_CHAT, "system",
                    "[SERVER]: Your experience points must not be negative!");
            return;
        }
        this.tanksServices.addScore(msg.localLobby, score);
    }

    private void addCry(ChatMessage msg, String[] arguments) {
        if (arguments.length < 2)
            return;
        this.tanksServices.addCrystall(msg.localLobby, this.getInt(arguments[1]));
    }

    private void stop(boolean stopped, String Chat_has_stopped) {
        this.stopped = stopped;
        this.sendSystemMessageToAll(
                Chat_has_stopped,
                false);
    }

    private void system(String[] arguments) {
        if (arguments.length < 2)
            return;
        this.sendSystemMessageToAll(arguments, false);
    }

    public void cleanMessagesByText(String text) {
        Predicate<ChatMessage> filter = p -> p.message.equals(text);
        this.chatMessages.removeIf(filter);
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clean_by_text", text);
    }

    public void cleanMessagesByUser(String nickname) {
        Predicate<ChatMessage> ifDelete = p -> !p.system && p.user != null && p.user.getNickname().equals(nickname);
        this.chatMessages.removeIf(ifDelete);
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clean_by", nickname);
    }

    public void clear() {
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clear_all");
        this.chatMessages.clear();
        this.sendSystemMessageToAll("The chat has been cleaned", false);
    }

    public void sendSystemMessageToAll(String[] ar, boolean yellow) {
        StringBuffer total = new StringBuffer();
        for (int i = 1; i < ar.length; ++i) {
            total.append(ar[i]).append(" ");
        }
        ChatMessage sys_msg = new ChatMessage(null, false, total.toString(), false, null, false, yellow, null);
        sys_msg.system = true;
        this.chatMessages.add(sys_msg);
        if (this.chatMessages.size() >= 50) {
            this.chatMessages.remove(0);
        }
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "system", total.toString().trim(),
                yellow ? "yellow" : "green");
    }

    public void sendSystemMessageToAll(String msg, boolean yellow) {
        ChatMessage sys_msg = new ChatMessage(null, false, msg, false, null, false, yellow, null);
        sys_msg.system = true;
        this.chatMessages.add(sys_msg);
        if (this.chatMessages.size() >= 50) {
            this.chatMessages.remove(0);
        }
        this.lobbyServices.sendCommandToAllUsersBesides(Type.LOBBY_CHAT, UserLocation.BATTLE, "system", msg.trim());
    }

    public void sendMessageToAll(ChatMessage msg) {
        this.lobbyServices.sendCommandToAllUsersBesides(Type.LOBBY_CHAT, UserLocation.BATTLE,
                JSONUtils.parseChatLobbyMessage(msg));
    }

    public String getNormalMessage(String src) {
        StringBuilder str = new StringBuilder();
        char[] mass = src.toCharArray();
        for (int i = 0; i < mass.length; ++i) {
            if (mass[i] == ' ') {
                if (mass[i] == mass[i + 1])
                    continue;
                str.append(" ");
                continue;
            }
            str.append(mass[i]);
        }
        mass = null;
        return str.toString();
    }

    private int getInt(String src) {
        try {
            return Integer.parseInt(src);
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }

    public Collection<ChatMessage> getMessages() {
        return this.chatMessages;
    }
}
