/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.spectator.chat;

import flashtanki.utils.StringUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.bonuses.BonusType;
import flashtanki.battles.chat.BattlefieldChatModel;
import flashtanki.battles.spectator.SpectatorController;
import flashtanki.battles.spectator.SpectatorModel;
import flashtanki.commands.Type;
import flashtanki.lobby.LobbyManager;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.services.BanServices;
import flashtanki.services.LobbysServices;
import flashtanki.services.ban.BanChatCommads;
import flashtanki.services.ban.BanTimeType;
import flashtanki.services.ban.BanType;
import flashtanki.services.ban.block.BlockGameReason;
import flashtanki.users.User;

public class SpectatorChatModel {
    private static final String CHAT_SPECTATOR_COMAND = "spectator_message";
    private SpectatorModel spModel;
    private BattlefieldModel bfModel;
    private BattlefieldChatModel chatModel;
    private DatabaseManager database = DatabaseManagerImpl.instance();
    private LobbysServices lobbyServices = LobbysServices.getInstance();

    private BanServices banServices = BanServices.getInstance();

    public SpectatorChatModel(SpectatorModel spModel) {
        this.spModel = spModel;
        this.bfModel = spModel.getBattleModel();
        this.chatModel = this.bfModel.chatModel;
    }

    public void onMessage(String message, SpectatorController spectator) {
        block40: {
            block38: {
                if (!message.startsWith("/"))
                    break block38;
                String[] arguments = message.replace('/', ' ').trim().split(" ");
                if (!spectator.getUser().getUserGroup().isAvaliableChatCommand(arguments[0])) {
                    this.chatModel.sendSystemMessage("Command unknown", spectator);
                    return;
                }
                switch (arguments[0]) {
                    case "system": {
                        StringBuffer total = new StringBuffer();
                        for (int i = 1; i < arguments.length; ++i) {
                            total.append(arguments[i]).append(" ");
                        }
                        this.chatModel.sendSystemMessage(total.toString());
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
                            break;
                        }

                        BanTimeType time = BanTimeType.FOREVER;
                        if(arguments.length>=4){
                            time = BanChatCommads.getTimeType(arguments[3]);
                        }

                        this.banServices.ban(BanType.GAME, time, victim_, spectator.getUser(),
                                BlockGameReason.getReasonById(reasonId).getReason());
                        LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
                        if (lobby != null) {
                            lobby.kick();
                        }
                        this.chatModel.sendSystemMessage(StringUtils.concatStrings(
                                "Tanker ", victim_.getNickname(),
                                " was blocked and kicked"));
                        break;
                    }
                    case "unban": {
                        User cu;
                        if (arguments.length < 2 || (cu = this.database.getUserByNickName(arguments[1])) == null)
                            break;
                        this.banServices.unbanChat(cu);
                        this.chatModel.sendSystemMessage("Tanker "
                                + cu.getNickname()
                                + " has been unbanned");
                        break;
                    }
                    case "unblockgame": {
                        if (arguments.length < 2) {
                            return;
                        }
                        User av = this.database.getUserByNickName(arguments[1]);
                        if (av == null)
                            break;
                        this.banServices.unblock(av);
                        this.chatModel.sendSystemMessage(String.valueOf(av.getNickname())
                                + " has been unblocked");
                        break;
                    }
                    case "spawngold": {
                        for (int i = 0; i < Integer.parseInt(arguments[1]); ++i) {
                            this.spModel.getBattleModel().bonusesSpawnService.spawnBonus(BonusType.GOLD);
                        }
                        break;
                    }
                    case "kick": {
                        LobbyManager _lobby;
                        User _userForKick = this.database.getUserByNickName(arguments[1]);
                        if (_userForKick == null || (_lobby = this.lobbyServices.getLobbyByUser(_userForKick)) == null)
                            break;
                        _lobby.kick();
                        this.chatModel.sendSystemMessage(
                                String.valueOf(_userForKick.getNickname()) + " has been kicked");
                        break;
                    }
                    case "w": {
                        if (arguments.length < 3) {
                            return;
                        }
                        User giver = this.database.getUserByNickName(arguments[1]);
                        if (giver == null)
                            break;
                        String reason = StringUtils.concatMassive(arguments, 2);
                        this.chatModel.sendSystemMessage(StringUtils.concatStrings(
                                "Tanker ", giver.getNickname(),
                                " has been warned. Reason: ",
                                reason));
                        break;
                    }
                }
                if (message.startsWith("/ban")) {
                    BanTimeType time = BanChatCommads.getTimeType(arguments[0]);
                    if (arguments.length < 3) {
                        return;
                    }
                    String reason = StringUtils.concatMassive(arguments, 2);
                    if (time == null) {
                        return;
                    }
                    User _victim = this.database.getUserByNickName(arguments[1]);
                    if (_victim == null) {
                        return;
                    }
                    this.banServices.ban(BanType.CHAT, time, _victim, spectator.getUser(), reason);
                    this.chatModel.sendSystemMessage(StringUtils.concatStrings(
                            "Tanker ", _victim.getNickname(),
                            " has been muted from the chat ",
                            time.getNameType(), " Reason: ", reason));
                }
                break block40;
            }
            this.spModel.getBattleModel().sendToAllPlayers(Type.BATTLE, CHAT_SPECTATOR_COMAND, message);
        }
    }
}
