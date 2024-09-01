/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.chat;

import flashtanki.utils.StringUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.bonuses.BonusType;
import flashtanki.battles.spectator.SpectatorController;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.lobby.LobbyManager;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.main.params.OnlineStats;
import flashtanki.premium.PremiumService;
import flashtanki.services.BanServices;
import flashtanki.services.LobbysServices;
import flashtanki.services.TanksServices;
import flashtanki.services.ban.BanChatCommads;
import flashtanki.services.ban.BanTimeType;
import flashtanki.services.ban.BanType;
import flashtanki.services.ban.DateFormater;
import flashtanki.services.ban.block.BlockGameReason;
import flashtanki.users.TypeUser;
import flashtanki.users.User;
import flashtanki.users.karma.Karma;

import java.util.Date;

public class BattlefieldChatModel {
    private BattlefieldModel bfModel;
    private static final TanksServices tanksServices = TanksServices.getInstance();

    private static final DatabaseManager database = DatabaseManagerImpl.instance();

    private static final LobbysServices lobbyServices = LobbysServices.getInstance();

    private static final BanServices banServices = BanServices.getInstance();
    private static final PremiumService premiumService = PremiumService.getInstance();

    public BattlefieldChatModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void onMessage(BattlefieldPlayerController player, String message, boolean team) {
        block73:
        {
            block71:
            {
                if ((message = message.trim()).isEmpty()) {
                    return;
                }
                Karma karma = this.database.getKarmaByUser(player.getUser());
                if (karma.isChatBanned()) {
                    Date banTo;
                    long currDate = System.currentTimeMillis();
                    long delta = currDate - (banTo = karma.getChatBannedBefore()).getTime();
                    if (delta <= 0L) {
                        player.parentLobby.send(Type.LOBBY_CHAT, "system", StringUtils.concatStrings(
                                "You are disconnected from the chat. You will be unmuted in ",
                                DateFormater.formatTimeToUnban(delta),
                                ". Reason: " + karma.getReasonChatBan()));
                        return;
                    }
                    this.banServices.unbanChat(player.getUser());
                }
                if (!this.bfModel.battleInfo.team) {
                    team = false;
                }
                if (!message.startsWith("/"))
                    break block71;
                String[] arguments = message.replace('/', ' ').trim().split(" ");
                switch (arguments[0]) {
                    case "vote": {
                        if (arguments.length < 2) {
                            this.sendSystemMessage("Not enough arguments", player);
                            return;
                        }
                        this.sendSystemMessage("Complaint sent succesfully", player);
                        this.voteUser(arguments[1], player);
                        break;
                    }
                }
                if (!player.getUser().getUserGroup().isAvaliableChatCommand(arguments[0])) {
                    this.sendSystemMessage("Command unknown", player);
                    return;
                }
                if (player.getUser().getType() == TypeUser.DEFAULT) {
                    return;
                }
                switch (arguments[0]) {
                    case "system": {
                        StringBuffer total = new StringBuffer();
                        for (int i = 1; i < arguments.length; ++i) {
                            total.append(arguments[i]).append(" ");
                        }
                        this.sendSystemMessage(total.toString());
                        break;
                    }
                    case "addcry": {
                        this.tanksServices.addCrystall(player.parentLobby, this.getInt(arguments[1]));
                        break;
                    }
                    case "addscore": {
                        int score = this.getInt(arguments[1]);
                        if (player.parentLobby.getLocalUser().getScore() + score < 0) {
                            this.sendSystemMessage("[SERVER]: Your number of experience points should not be negative!",
                                    player);
                            break;
                        }
                        this.tanksServices.addScore(player.parentLobby, score);
                        break;
                    }
                    case "blockgame": {
                        if (arguments.length < 3) {
                            return;
                        }
                        User victim_ = this.database.getUserByNickName(arguments[1]);
                        int reasonId = 0;
                        try {
                            reasonId = Integer.parseInt(arguments[2]);
                        } catch (Exception ex) {
                            reasonId = 0;
                        }
                        if (victim_ == null) {
                            this.sendSystemMessage(
                                    "[SERVER]: Player not found!",
                                    player);
                            break;
                        }
                        BanTimeType time = BanTimeType.FOREVER;
                        if (arguments.length >= 4) {
                            time = BanChatCommads.getTimeType(arguments[3]);
                        }

                        this.banServices.ban(BanType.GAME, time, victim_, player.getUser(),
                                BlockGameReason.getReasonById(reasonId).getReason());
                        LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
                        if (lobby != null) {
                            lobby.kick();
                        }
                        this.sendSystemMessage(StringUtils.concatStrings("Tanker ",
                                victim_.getNickname(),
                                " was blocked and kicked"));
                        break;
                    }
                    case "unban": {
                        if (arguments.length < 2)
                            break;
                        User cu = this.database.getUserByNickName(arguments[1]);
                        if (cu == null) {
                            this.sendSystemMessage(
                                    "[SERVER]: Player not found!",
                                    player);
                            break;
                        }
                        this.banServices.unbanChat(cu);
                        this.sendSystemMessage("Tanker " + cu.getNickname()
                                + " has been unbanned");
                        break;
                    }
                    case "unblockgame": {
                        if (arguments.length < 2) {
                            return;
                        }
                        User av = this.database.getUserByNickName(arguments[1]);
                        if (av == null) {
                            this.sendSystemMessage(
                                    "[SERVER]: User not found!",
                                    player);
                            break;
                        }
                        this.banServices.unblock(av);
                        this.sendSystemMessage(av.getNickname()
                                + " unlocked");
                        break;
                    }
                    case "spawngold": {
                        for (int i = 0; i < Integer.parseInt(arguments[1]); ++i) {
                            this.bfModel.bonusesSpawnService.spawnBonus(BonusType.GOLD);
                        }
                        break;
                    }
                    case "kick": {
                        User _userForKick = this.database.getUserByNickName(arguments[1]);
                        if (_userForKick == null) {
                            this.sendSystemMessage(
                                    "[SERVER]: Player not found",
                                    player);
                            break;
                        }
                        LobbyManager _lobby = this.lobbyServices.getLobbyByUser(_userForKick);
                        if (_lobby == null)
                            break;
                        _lobby.kick();
                        this.sendSystemMessage(_userForKick.getNickname() + " kicked");
                        break;
                    }
                    case "online": {
                        this.sendSystemMessage("Current online: " + OnlineStats.getOnline() + "\nMax online: "
                                + OnlineStats.getMaxOnline(), player);
                        break;
                    }
                    case "w": {
                        if (arguments.length < 3) {
                            return;
                        }
                        User giver = this.database.getUserByNickName(arguments[1]);
                        if (giver == null) {
                            this.sendSystemMessage("[SERVER]: Player not found\u0435\u043d!", player);
                            break;
                        }
                        String reason = StringUtils.concatMassive(arguments, 2);
                        this.sendSystemMessage(StringUtils.concatStrings("Tanker ", giver.getNickname(),
                                " has been warned. Reason: ", reason));
                        break;
                    }
                    case "getip": {
                        if (arguments.length < 2)
                            break;
                        User shower = this.database.getUserByNickName(arguments[1]);
                        if (shower == null) {
                            return;
                        }
                        String ip = shower.getAntiCheatData().ip;
                        if (ip == null) {
                            ip = shower.getLastIP();
                        }
                        this.sendSystemMessage("IP user " + shower.getNickname() + " : " + ip, player);
                        break;
                    }
                    default: {
                        if (message.startsWith("/ban"))
                            break;
                        // this.sendSystemMessage(
                        //         "[SERVER]: Unknown team!",
                        //         player);
                    }
                }
                if (message.startsWith("/ban")) {
                    BanTimeType time = BanChatCommads.getTimeType(arguments[0]);
                    if (arguments.length < 3) {
                        return;
                    }
                    String reason = StringUtils.concatMassive(arguments, 2);
                    if (time == null) {
                        this.sendSystemMessage(
                                "[SERVER]: Ban command not found!",
                                player);
                        return;
                    }
                    User _victim = this.database.getUserByNickName(arguments[1]);
                    if (_victim == null) {
                        this.sendSystemMessage(
                                "[SERVER]: Player not found!",
                                player);
                        return;
                    }
                    this.banServices.ban(BanType.CHAT, time, _victim, player.getUser(), reason);
                    this.sendSystemMessage(StringUtils.concatStrings("Tanker ",
                            _victim.getNickname(),
                            " has been muted from the chat ",
                            time.getNameType(), " Reason: ", reason));
                }
                break block73;
            }
            if (message.length() >= 399) {
                message = null;
                return;
            }
            if (!player.parentLobby.getChatFloodController().detected(message)) {
                this.sendMessage(new BattleChatMessage(player.getUser().getId(), player.getUser().getNickname(),
                        premiumService.getPremiumTime(player.getUser().getId()).isActivated(),
                        player.getUser().getRang(),
                        message, player.playerTeamType, team, false));
            } else {
                if (player.getUser().getWarnings() >= 5) {
                    BanTimeType time = BanTimeType.FIVE_MINUTES;
                    String reason = "Flood.";
                    this.banServices.ban(BanType.CHAT, time, player.getUser(), player.getUser(), reason);
                    this.sendSystemMessage(StringUtils.concatStrings("Tanker ",
                            player.getUser().getNickname(),
                            " has been muted from the chat ",
                            time.getNameType(), " Reason: ", reason));
                    return;
                }
                this.sendSystemMessage("Tanker " + player.getUser().getNickname()
                        + "  warned Reason: Flood.");
                player.getUser().addWarning();
            }
        }
    }

    public void voteUser(String nickname, BattlefieldPlayerController player) {
        User userReported = LobbysServices.getInstance().getLobbyByNick(nickname).getLocalUser();
        for (String userNick : OnlineStats.onlinePlayers) {
            LobbyManager userLobby = LobbysServices.getInstance().getLobbyByNick(userNick);
            if (userLobby.getLocalUser().getType() != TypeUser.DEFAULT) {
                if (userLobby.battle == null) {
                    LobbyManager reportedUserLobby = LobbysServices.getInstance().getLobbyByUser(userReported);
                    userLobby.send(Type.LOBBY_CHAT, "system",
                            StringUtils.concatStrings("[REPORT]: User: ", userReported.getNickname(),
                                    " in battle: #battle", reportedUserLobby.battle.battle.battleInfo.battleId,
                                    "\nVoted by: " + player.getUser().getNickname()));
                }
            }
        }
    }

    public void sendSystemMessage(String message) {
        if (message == null) {
            message = " ";
        }
        this.sendMessage(new BattleChatMessage(null,null, false, 0, message, "NONE", false, true));
    }

    public void sendSystemMessage(String message, BattlefieldPlayerController player) {
        if (message == null) {
            message = " ";
        }
        this.sendMessage(new BattleChatMessage(null,null, false, 0, message, "NONE", false, true), player);
    }

    public void sendSystemMessage(String message, SpectatorController player) {
        if (message == null) {
            message = " ";
        }
        this.sendMessage(new BattleChatMessage(null,null, false, 0, message, "NONE", false, true), player);
    }

    private void sendMessage(BattleChatMessage msg) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
    }

    private void sendMessage(BattleChatMessage msg, BattlefieldPlayerController controller) {
        controller.send(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
    }

    private void sendMessage(BattleChatMessage msg, SpectatorController controller) {
        this.lobbyServices.getLobbyByUser(controller.getUser()).send(Type.BATTLE, "chat", JSONUtils.parseBattleChatMessage(msg));
    }

    public int getInt(String src) {
        try {
            return Integer.parseInt(src);
        } catch (Exception ex) {
            return Integer.MAX_VALUE;
        }
    }
}
