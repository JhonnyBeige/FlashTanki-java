/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons.flamethrower;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.IWeapon;
import flashtanki.battles.tanks.weapons.anticheats.TickableWeaponAnticheatModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FlamethrowerModel
extends TickableWeaponAnticheatModel
implements IWeapon {
    private FlamethrowerEntity entity;
    public BattlefieldModel bfModel;
    public BattlefieldPlayerController player;

    public FlamethrowerModel(FlamethrowerEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.targetDetectionInterval);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void startFire(String json) {
        this.bfModel.startFire(this.player);
    }

    @Override
    public void stopFire() {
        this.bfModel.stopFire(this.player);
    }

    @Override
    public void fire(String json) {
        try {
            JSONObject parser = (JSONObject)new JSONParser().parse(json);
            JSONArray arrayTanks = (JSONArray)parser.get("targetsIds");
            if (!this.check((int)((Long)parser.get("tickPeriod")).longValue())) {
                this.bfModel.cheatDetected(this.player, this.getClass());
                return;
            }
            if (arrayTanks.size() == 0) {
                return;
            }
            BattlefieldPlayerController[] targetVictim = new BattlefieldPlayerController[arrayTanks.size()];
            for (int i = 0; i < arrayTanks.size(); ++i) {
                BattlefieldPlayerController target = this.bfModel.getPlayer((String)arrayTanks.get(i));
                if (target == null || (float)((int)(target.tank.position.distanceTo(this.player.tank.position) / 100.0)) > this.entity.range) continue;
                targetVictim[i] = target;
            }
            this.onTarget(targetVictim, 0);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        BattlefieldPlayerController[] arrbattlefieldPlayerController = targetsTanks;
        int n = targetsTanks.length;
        for (int i = 0; i < n; ++i) {
            BattlefieldPlayerController victim = arrbattlefieldPlayerController[i];
            this.bfModel.tanksKillModel.damageTank(victim, this.player, this.entity.damage_max, true);
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    public void quickFire(String var1) {
    }
}

