/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.system.missions.dailybonus.crystalls;

import flashtanki.lobby.LobbyManager;
import flashtanki.services.TanksServices;

import java.util.List;

public class CrystallsBonusModel {
    private static final TanksServices tanksServices = TanksServices.getInstance();
    private static final List<Integer> CRYSTALLS=
            List.of(0, 0, 15, 25, 35, 50, 60, 75, 85, 95,
                    110, 120, 135, 145, 155, 170, 180, 195, 205, 215,
                    230, 240, 255, 265, 275, 290, 300, 300, 300, 300, 300);

    public int applyBonus(LobbyManager lobby) {
        int rang = lobby.getLocalUser().getRang();
        int bonus = CRYSTALLS.get(rang);
        tanksServices.addCrystall(lobby, bonus);
        return bonus;
    }
}

