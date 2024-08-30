/*
 * Decompiled with CFR 0.150.
 */
package gtanks.lobby.chat;

import gtanks.lobby.LobbyManager;
import gtanks.users.User;

public class ChatMessage {
    public User user;
    public Boolean isPremium;
    public String message;
    public boolean addressed;
    public boolean system;
    public User userTo;
    public Boolean isPremiumTo;
    public LobbyManager localLobby;
    public boolean yellowMessage;

    public ChatMessage(User user, Boolean isPremium, String message, boolean addressed, User userTo, boolean isPremiumTo, boolean yellowMessage, LobbyManager localLobby) {
        this.user = user;
        this.isPremium = isPremium;
        this.message = message;
        this.addressed = addressed;
        this.userTo = userTo;
        this.isPremiumTo = isPremiumTo;
        this.yellowMessage = yellowMessage;
        this.localLobby = localLobby;
    }

    public ChatMessage(User user, Boolean isPremium, String message, boolean addressed, User userTo, Boolean isPremiumTo, LobbyManager localLobby) {
        this.user = user;
        this.isPremium = isPremium;
        this.message = message;
        this.addressed = addressed;
        this.userTo = userTo;
        this.isPremiumTo = isPremiumTo;
        this.localLobby = localLobby;
    }

    public String toString() {
        return String.valueOf(this.system ? "SYSTEM: " : String.valueOf(this.user.getNickname()) + ": ") + (this.addressed ? "->" + this.userTo.getNickname() : "") + this.message;
    }
}

