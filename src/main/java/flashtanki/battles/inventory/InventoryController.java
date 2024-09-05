/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.inventory;

import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.effects.Effect;
import flashtanki.battles.effects.EffectType;
import flashtanki.battles.effects.impl.ArmorEffect;
import flashtanki.battles.effects.impl.DamageEffect;
import flashtanki.battles.effects.impl.HealthEffect;
import flashtanki.battles.effects.impl.Mine;
import flashtanki.battles.effects.impl.NitroEffect;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.logger.remote.types.LogType;
import flashtanki.logger.LoggerService;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.users.garage.items.Item;

import java.util.Map;

public class InventoryController {
    private static final String INIT_INVENTORY_COMAND = "init_inventory";
    private static final String ACTIVATE_ITEM_COMAND = "activate_item";
    private static final String ENABLE_EFFECT_COMAND = "enable_effect";
    private static final String REFRESH_INVENTORY_COMAND = "update_inventory";
    private static DatabaseManager database = DatabaseManagerImpl.instance();
    private static LoggerService loggerService = LoggerService.getInstance();
    private BattlefieldPlayerController player;

    public static Map<EffectType, Map<EffectType, Long>> lockTime = Map.of(
            EffectType.ARMOR, Map.of(EffectType.ARMOR, 59L, EffectType.DAMAGE, 10L, EffectType.HEALTH, 0L, EffectType.NITRO, 5L),
            EffectType.DAMAGE, Map.of(EffectType.ARMOR, 10L, EffectType.DAMAGE, 59L, EffectType.HEALTH, 0L, EffectType.NITRO, 5L),
            EffectType.HEALTH, Map.of(EffectType.ARMOR, 0L, EffectType.DAMAGE, 0L, EffectType.HEALTH, 0L, EffectType.NITRO, 0L),
            EffectType.NITRO, Map.of(EffectType.ARMOR, 5L, EffectType.DAMAGE, 5L, EffectType.HEALTH, 0L, EffectType.NITRO, 59L)
    );


    public InventoryController(BattlefieldPlayerController player) {
        this.player = player;
    }

    public void init() {
        if(!this.player.battle.battleInfo.isPaid){
            this.player.send(Type.BATTLE, INIT_INVENTORY_COMAND, JSONUtils.parseInitInventoryComand(this.player.getGarage()));
        }else{
            this.player.send(Type.BATTLE, INIT_INVENTORY_COMAND, JSONUtils.parseDisabledInventory(this.player.getGarage()));
        }
    }
    
    public void refresh() {
        if(!this.player.battle.battleInfo.isPaid){
            this.player.send(Type.BATTLE, REFRESH_INVENTORY_COMAND, JSONUtils.parseInitInventoryComand(this.player.getGarage()));
        }else{
            this.player.send(Type.BATTLE, REFRESH_INVENTORY_COMAND, JSONUtils.parseDisabledInventory(this.player.getGarage()));
        }
    }

    public void activateItem(String id, Vector3 tankPos) {
         this.player.getGarage().getItemById(id)
                 .filter(item -> item.count > 0)
                 .ifPresent(item -> {
                     Effect effect = getEffectById(id);

                     if (!this.player.tank.isUsedEffect(effect.getEffectType()) && canActivate(effect.getEffectType())) {
                         effect.activate(this.player, true, tankPos);
                         this.onActivatedItem(item, effect.getDurationTime());
                         --item.count;
                         if (item.count <= 0) {
                             this.player.getGarage().items.remove(item);
                         }
                         new Thread(() -> {
                             this.player.getGarage().parseJSONData();
                             database.update(this.player.getGarage());
                         }).start();
                     }
                 });
    }

    public boolean canActivate(EffectType effectType) {
        //lock > currnetTimeSeconds
        if(player.tank.lockEffects.getOrDefault(effectType,0L) > System.currentTimeMillis()){
            return false;
        }else {
            lockTime.getOrDefault(effectType, Map.of()).forEach((key, value) -> {
                player.tank.lockEffects.put(key, System.currentTimeMillis() + value * 1000);
            });
            return true;
        }
    }

    private void onActivatedItem(Item item, int durationTime) {
        this.player.send(Type.BATTLE, ACTIVATE_ITEM_COMAND, item.id);
        this.player.battle.sendToAllPlayers(Type.BATTLE, ENABLE_EFFECT_COMAND, this.player.getUser().getNickname(), String.valueOf(item.index), String.valueOf(durationTime));
    }

    private Effect getEffectById(String id) {
        Effect effect = null;
        switch (id) {
            case "armor": {
                effect = new ArmorEffect();
                break;
            }
            case "double_damage": {
                effect = new DamageEffect();
                break;
            }
            case "n2o": {
                effect = new NitroEffect();
                break;
            }
            case "health": {
                effect = new HealthEffect();
                break;
            }
            case "mine": {
                effect = new Mine();
                break;
            }
            default: {
                loggerService.log(LogType.INFO,"Effect with id:" + id + " not found!");
            }
        }
        return effect;
    }
}

