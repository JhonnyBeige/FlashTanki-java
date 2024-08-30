/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.statistic;

import flashtanki.logger.RemoteDatabaseLogger;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerStatistic implements Comparable<PlayerStatistic> {
    private long kills;
    private int deaths;
    @Setter
    private int prize;
    private long score;

    public PlayerStatistic(int kills, int deaths, int score) {
        this.kills = kills;
        this.deaths = deaths;
        this.score = score;
    }

    public void addKills() {
        ++this.kills;
    }

    public void addDeaths() {
        ++this.deaths;
    }

    public void addScore(int value) {
        if (value <= 0) {
            RemoteDatabaseLogger.error( new Exception("Score value must be greater than 0"));
        }
        this.score += value;
    }

    public void clear() {
        this.kills = 0L;
        this.deaths = 0;
        this.prize = 0;
        this.score = 0L;
    }
    @Override
    public int compareTo(PlayerStatistic arg0) {
        return (int) (arg0.getScore() - this.score);
    }
}

