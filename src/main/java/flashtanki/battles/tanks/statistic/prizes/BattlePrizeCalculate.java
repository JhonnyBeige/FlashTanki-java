/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.statistic.prizes;

import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.tanks.statistic.PlayerStatistic;
import flashtanki.services.TanksServices;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BattlePrizeCalculate {
    private final TanksServices tankServices = TanksServices.getInstance();

    public  void calc(List<BattlefieldPlayerController> users, int fund) {
        if (users == null || users.size() == 0) {
            return;
        }
        BattlefieldPlayerController _first = Collections.max(users, new Comparator<BattlefieldPlayerController>(){

            @Override
            public int compare(BattlefieldPlayerController o1, BattlefieldPlayerController o2) {
                return (int)(o1.statistic.getScore() - o2.statistic.getScore());
            }
        });
        PlayerStatistic first = _first.statistic;
        double sumSquare = 0.0;
        int countFirstUsers = 0;
        for (BattlefieldPlayerController user : users) {
            long value = user.statistic.getScore();
            if (value != first.getScore()) {
                sumSquare += (double)(value * value);
                continue;
            }
            ++countFirstUsers;
        }
        sumSquare += (double)(first.getScore() * first.getScore() * (long)countFirstUsers * (long)countFirstUsers);
        int allSum = 0;
        for (BattlefieldPlayerController user : users) {
            if (user.statistic.getScore() == first.getScore()) continue;
            int prize = (int)((double)((long)fund * user.statistic.getScore() * user.statistic.getScore()) / sumSquare);
            if (prize < 0) {
                prize = Math.abs(prize);
            }
            allSum += prize;
            user.statistic.setPrize(prize);
            tankServices.addCrystall(user.parentLobby, prize);
        }
        int delta = (fund - allSum) / countFirstUsers;
        for (BattlefieldPlayerController user : users) {
            PlayerStatistic _user = user.statistic;
            if (_user.getScore() != first.getScore() || user == _first) continue;
            _user.setPrize(delta);
            tankServices.addCrystall(user.parentLobby, delta);
            allSum += delta;
        }
        first.setPrize(first.getPrize() + (fund - allSum));
        tankServices.addCrystall(_first.parentLobby, first.getPrize());
    }

    public void calculateForTeam(ArrayList<BattlefieldPlayerController> redUsers, ArrayList<BattlefieldPlayerController> blueUsers, int scoreRed, int scoreBlue, double looseKoeff, int fund) {
        ArrayList<BattlefieldPlayerController> usersLoose;
        ArrayList<BattlefieldPlayerController> usersWin;
        int prizeLoose = 0;
        int prizeWin = 0;
        if (scoreRed != scoreBlue) {
            int scoreWin = Math.max(scoreRed, scoreBlue);
            int scoreLoose = Math.min(scoreRed, scoreBlue);
            
            double winDiv = 1.6f;
            double looseDiv = 2.4f;

            prizeWin = (int)Math.ceil((float)fund / winDiv);
            prizeLoose = (int)Math.ceil((float)fund / looseDiv);

            usersWin = scoreRed > scoreBlue ? redUsers : blueUsers;
            usersLoose = scoreRed > scoreBlue ? blueUsers : redUsers;
        } else {
            prizeLoose = (int)Math.ceil((float)fund / 2.0f);
            prizeWin = (int)Math.ceil((float)fund / 2.0f);
            usersWin = redUsers;
            usersLoose = blueUsers;
        }
        calc(usersWin, prizeWin);
        calc(usersLoose, prizeLoose);
    }
}

