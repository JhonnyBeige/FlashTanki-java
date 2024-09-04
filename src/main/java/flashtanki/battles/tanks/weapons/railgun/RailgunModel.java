/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons.railgun;

import flashtanki.utils.RandomUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.anticheats.AnticheatModel;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.IWeapon;
import flashtanki.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@AnticheatModel(name="RailgunModel", actionInfo="Child FireableWeaponAnticheatModel")
public class RailgunModel
extends FireableWeaponAnticheatModel
implements IWeapon {
    private final RailgunEntity entity;
    private final BattlefieldModel battle;
    private final BattlefieldPlayerController tank;

    public RailgunModel(RailgunEntity entity, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.battle = battle;
        this.tank = tank;
    }

    @Override
    public void startFire(String json) {
        this.battle.startFire(this.tank);
    }

    @Override
    public void fire(String json_info) {
        this.battle.fire(this.tank, json_info);
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(json_info);
            JSONArray tanks = (JSONArray)json.get("targets");
            if (!this.check((int)((Long)json.get("reloadTime")).longValue())) {
                this.battle.cheatDetected(this.tank, this.getClass());
                return;
            }
            if (tanks == null) {
                return;
            }
            BattlefieldPlayerController[] tanks_array = new BattlefieldPlayerController[tanks.size()];
            for (int i = 0; i < tanks.size(); ++i) {
                tanks_array[i] = this.battle.players.get(tanks.get(i));
            }
            this.onTarget(tanks_array, 0);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (targetsTanks.length == 0) {
            return;
        }
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        for (int i = 0; i < targetsTanks.length; ++i) {
            this.battle.tanksKillModel.damageTank(targetsTanks[i], this.tank, damage, true);
            damage /= 2.0f;
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void stopFire() {
    }

    public void quickFire(String var1) {
    }
}

