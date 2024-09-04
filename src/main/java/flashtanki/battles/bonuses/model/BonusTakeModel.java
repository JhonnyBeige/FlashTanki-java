/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.bonuses.model;

import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.bonuses.Bonus;
import flashtanki.battles.effects.Effect;
import flashtanki.battles.effects.EffectType;
import flashtanki.battles.effects.impl.ArmorEffect;
import flashtanki.battles.effects.impl.DamageEffect;
import flashtanki.battles.effects.impl.HealthEffect;
import flashtanki.battles.effects.impl.NitroEffect;
import flashtanki.commands.Type;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;

public class BonusTakeModel {
    private static final String SET_CRY = "set_cry";
    private static final String ENABLE_EFFECT_COMAND = "enable_effect";
    private static final int GOLD_BONUS_COST = 1000;
    private static final DatabaseManager database = DatabaseManagerImpl.instance();
    private static BonusTakeModel instance;

    private BonusTakeModel() {
    }

    public static BonusTakeModel getInstance() {
        if (instance == null) {
            instance = new BonusTakeModel();
        }
        return instance;
    }

    public boolean onTakeBonus(Bonus bonus, BattlefieldPlayerController player) {
        player.battle.bonusesSpawnService.removeRegion(bonus.bonusRegion);
        switch (bonus.type) {
            case GOLD: {
                player.battle.sendUserLogMessage(player.parentLobby.getLocalUser().getNickname(),
                        "has taken the gold box");
                player.parentLobby.getLocalUser().addCrystall(GOLD_BONUS_COST);
                player.send(Type.BATTLE, SET_CRY, String.valueOf(player.parentLobby.getLocalUser().getCrystall()));
                database.update(player.getUser());
                player.battle.sendToAllPlayers(Type.BATTLE, "remove_graffiti;" + bonus.id);
                break;
            }
            case ARMOR: {
                this.activateDrop(new ArmorEffect(), player);
                break;
            }
            case DAMAGE: {
                this.activateDrop(new DamageEffect(), player);
                break;
            }
            case HEALTH: {
                this.activateDrop(new HealthEffect(), player);
                break;
            }
            case NITRO: {
                this.activateDrop(new NitroEffect(), player);
            }
        }
        return true;
    }

    private void activateDrop(Effect effect, BattlefieldPlayerController player) {
        effect.activate(player, false, player.tank.position);
        player.battle.sendToAllPlayers(Type.BATTLE, ENABLE_EFFECT_COMAND, player.getUser().getNickname(),
                String.valueOf(effect.getID()),
                effect.getEffectType() == EffectType.HEALTH ? String.valueOf(10000) : String.valueOf(60000));
    }
}
