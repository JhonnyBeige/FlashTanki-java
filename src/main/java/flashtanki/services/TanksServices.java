/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.services;

import flashtanki.utils.RankUtils;
import flashtanki.commands.Type;
import flashtanki.lobby.LobbyManager;
import flashtanki.logger.remote.RemoteDatabaseLogger;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.users.User;

public class TanksServices {
    private static TanksServices instance = new TanksServices();
    private final DatabaseManager database = DatabaseManagerImpl.instance();

    public static TanksServices getInstance() {
        return instance;
    }

    public void addScore(LobbyManager lobby, int score) {
        boolean fall;
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::addScore: lobby null!");
            return;
        }
        User user = lobby.getLocalUser();
        if (user == null) {
            RemoteDatabaseLogger.error("TanksServices::addScore: user null!");
            return;
        }
        user.addScore(score);
        if(lobby.battle != null){
            lobby.battle.statistic.addScore(score);
        }
        boolean increase = user.getScore() >= user.getNextScore();
        boolean bl = fall = user.getScore() < RankUtils.getRankByIndex((int)user.getRang()).min;
        if (increase || fall) {
            if(lobby.battle != null){
                if (lobby.battle.battle != null) {
                    if (lobby.battle.battle.battleInfo != null) {
                        if(!bl && user.getRang() != 29){
                            lobby.battle.battle.sendToAllPlayers(Type.BATTLE, "create_levelup_effect", user.getNickname(), String.valueOf(user.getRang() + 2));
                        }
                    }
                }
            }
            user.setRang(RankUtils.getNumberRank(RankUtils.getRankByScore(user.getScore())));
            user.setNextScore(user.getRang() == 29 ? RankUtils.getRankByIndex((int)user.getRang()).max : RankUtils.getRankByIndex((int)user.getRang()).max + 1);
            lobby.send(Type.LOBBY, "update_rang_progress", String.valueOf(10000));
            lobby.send(Type.LOBBY, "update_rang", String.valueOf(user.getRang() + 1), String.valueOf(user.getNextScore()));
            addCrystall(lobby, RankUtils.rankupRewards[user.getRang()]);
        }
        int update = RankUtils.getUpdateNumber(user.getScore());
        lobby.send(Type.LOBBY, "update_rang_progress", String.valueOf(update));
        lobby.send(Type.LOBBY, "add_score", String.valueOf(user.getScore()));
        this.database.update(user);
    }

    public void addCrystall(LobbyManager lobby, int crystall) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::addCrystall: lobby null!");
            return;
        }
        User user = lobby.getLocalUser();
        if (user == null) {
            RemoteDatabaseLogger.error("TanksServices::addCrystall: user null!");
            return;
        }
        user.addCrystall(crystall);
        lobby.send(Type.LOBBY, "add_crystall", String.valueOf(user.getCrystall()));
        this.database.update(user);
    }

    public void dummyAddCrystall(LobbyManager lobby, int crystall) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::dummyAddCrystall: lobby null!");
            return;
        }
        User user = lobby.getLocalUser();
        if (user == null) {
            RemoteDatabaseLogger.error("TanksServices::dummyAddCrystall: user null!");
            return;
        }
        lobby.send(Type.LOBBY, "add_crystall", String.valueOf(user.getCrystall()));
    }
}

