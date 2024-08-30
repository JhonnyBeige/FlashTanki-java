/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.chat;

public class BattleChatMessage {
    public Long userId;
    public String nickname;
    public boolean isPremium;
    public int rank;
    public String message;
    public String teamType;
    public boolean team;
    public boolean system;

    public BattleChatMessage(Long userId, String nickname, boolean isPremium, int rank, String message, String teamType, boolean team, boolean system) {
        this.userId = userId;
        this.nickname = nickname;
        this.isPremium = isPremium;
        this.rank = rank;
        this.message = message.trim();
        this.teamType = teamType;
        this.team = team;
        this.system = system;
    }
}

