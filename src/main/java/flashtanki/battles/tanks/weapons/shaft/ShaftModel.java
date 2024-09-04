package flashtanki.battles.tanks.weapons.shaft;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.IWeapon;
import flashtanki.battles.tanks.weapons.WeaponWeakeningData;
import flashtanki.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;

import java.util.ArrayList;

import flashtanki.utils.RandomUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ShaftModel extends FireableWeaponAnticheatModel implements IWeapon {
   private final BattlefieldModel bfModel;
   private final BattlefieldPlayerController player;
   private final ShaftEntity entity;
   private final WeaponWeakeningData weakeingData;

   public ShaftModel(ShaftEntity entity, WeaponWeakeningData weakeingData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
      super(entity.getShotData().reloadMsec);
      this.entity = entity;
      this.bfModel = bfModel;
      this.player = player;
      this.weakeingData = weakeingData;
   }

   public void fire(String json) {
      JSONParser js = new JSONParser();
      JSONObject target = null;
      Number energy = 0;
      ArrayList ids = null;

      try {
         Object jo = js.parse(json);
         JSONObject jsonObj = (JSONObject)jo;
         JSONArray targets = (JSONArray)jsonObj.get("targets");
         energy = (Number)jsonObj.get("energy");
         ids = new ArrayList();

         for(int i = 0; i < targets.size(); ++i) {
            target = (JSONObject)targets.get(i);
            String id = target.get("id").toString();
            ids.add(id);
         }
      } catch (ParseException var12) {
         var12.printStackTrace();
      }

      this.bfModel.fire(this.player, json);
      BattlefieldPlayerController[] tanks_array = new BattlefieldPlayerController[ids.size()];
      if (target != null) {
         for(int i = 0; i < ids.size(); ++i) {
            tanks_array[i] = this.bfModel.players.get(ids.get(i));
         }
      }

      this.onTargetDamage(tanks_array, 0, energy.doubleValue());
   }

   public void quickFire(String json) {
      JSONParser js = new JSONParser();
      JSONObject target = null;
      ArrayList targetIds = null;

      try {
         Object jo = js.parse(json);
         JSONObject jsonObj = (JSONObject)jo;
         JSONArray targets = (JSONArray)jsonObj.get("targets");
         targetIds = new ArrayList();

         for(int i = 0; i < targets.size(); ++i) {
            target = (JSONObject)targets.get(i);
            String targetId = target.get("target_id").toString();
            targetIds.add(targetId);
         }
      } catch (ParseException var10) {
         var10.printStackTrace();
      }

      this.bfModel.quickFire(this.player, json);
      BattlefieldPlayerController[] tanks_array = new BattlefieldPlayerController[targetIds.size()];
      if (target != null) {
         for(int i = 0; i < targetIds.size(); ++i) {
            tanks_array[i] = this.bfModel.players.get(targetIds.get(i));
         }
      }

      this.onTargetQuickShot(tanks_array, 0);
   }

   public void startFire(String json) {
   }

   public void onTargetDamage(BattlefieldPlayerController[] targetsTanks, int distance, double energy) {
      if (targetsTanks.length != 0) {
         if (energy < 0.0D) {
            String var10000 = this.player.getUser().getNickname();
            this.bfModel.cheatDetected(this.player, this.getClass());
            return;
         }

         float damage = (float)((double)this.entity.fov_damage_max * ((double)1 - energy));

         for(int i = 0; i < targetsTanks.length; ++i) {
            this.bfModel.tanksKillModel.damageTank(targetsTanks[i], this.player, damage, true);
            damage /= 2.0F;
         }
      }
   }

   public void onTargetQuickShot(BattlefieldPlayerController[] targetsTanks, int distance) {
      if (targetsTanks.length != 0) {
         float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);

         for(int i = 0; i < targetsTanks.length; ++i) {
            this.bfModel.tanksKillModel.damageTank(targetsTanks[i], this.player, damage, true);
            damage /= 2.0F;
         }
      }

   }

   public IEntity getEntity() {
      return this.entity;
   }

   public void stopFire() {
   }

   public void onTarget(BattlefieldPlayerController[] var1, int var2) {
   }
}