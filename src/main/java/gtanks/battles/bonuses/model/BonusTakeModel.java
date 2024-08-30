/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.bonuses.model;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.bonuses.Bonus;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.impl.ArmorEffect;
import gtanks.battles.effects.impl.DamageEffect;
import gtanks.battles.effects.impl.HealthEffect;
import gtanks.battles.effects.impl.NitroEffect;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;

public class BonusTakeModel {
    private static final String SET_CRY = "set_cry";
    private static final String ENABLE_EFFECT_COMAND = "enable_effect";
    private static final int CRYSTALL_BONUS_COST = 10;
    private static final int GOLD_BONUS_COST = 1000;
    private static DatabaseManager database = DatabaseManagerImpl.instance();
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
            case CRYSTALL: {
                player.parentLobby.getLocalUser().addCrystall(CRYSTALL_BONUS_COST);
                player.send(Type.BATTLE, SET_CRY, String.valueOf(player.parentLobby.getLocalUser().getCrystall()));
                this.database.update(player.getUser());
                break;
            }
            case GOLD: {
                player.battle.sendUserLogMessage(player.parentLobby.getLocalUser().getNickname(),
                        "has taken the gold box");
                player.parentLobby.getLocalUser().addCrystall(GOLD_BONUS_COST);
                player.send(Type.BATTLE, SET_CRY, String.valueOf(player.parentLobby.getLocalUser().getCrystall()));
                this.database.update(player.getUser());
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
