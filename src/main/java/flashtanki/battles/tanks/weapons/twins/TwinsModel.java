/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons.twins;

import flashtanki.utils.RandomUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.anticheats.AnticheatModel;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.IWeapon;
import flashtanki.battles.tanks.weapons.WeaponUtils;
import flashtanki.battles.tanks.weapons.WeaponWeakeningData;
import flashtanki.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import flashtanki.commands.Type;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@AnticheatModel(name="TwinsModel", actionInfo="Child FireableWeaponAnticheatModel")
public class TwinsModel
extends FireableWeaponAnticheatModel
implements IWeapon {
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private WeaponWeakeningData weakeingData;
    private TwinsEntity entity;

    public TwinsModel(TwinsEntity twinsEntity, WeaponWeakeningData weakeingData, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(twinsEntity.getShotData().reloadMsec);
        this.bfModel = battle;
        this.player = tank;
        this.entity = twinsEntity;
        this.weakeingData = weakeingData;
    }

    @Override
    public void startFire(String json) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire_twins", this.player.tank.id, json);
    }

    @Override
    public void fire(String json) {
        this.bfModel.fire(this.player, json);
        try {
            JSONObject parser = (JSONObject)new JSONParser().parse(json);
            if (!this.check((int)((Long)parser.get("reloadTime")).longValue())) {
                this.bfModel.cheatDetected(this.player, this.getClass());
                return;
            }
            BattlefieldPlayerController victim = this.bfModel.getPlayer((String)parser.get("victimId"));
            this.onTarget(new BattlefieldPlayerController[]{victim}, (int)((Long)parser.get("distance")).longValue());
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        if (targetsTanks.length == 0) {
            return;
        }
        if (targetsTanks[0] == null) {
            return;
        }
        if ((double)distance >= this.weakeingData.minimumDamageRadius) {
            damage = WeaponUtils.calculateDamageFromDistance(damage, (int)this.weakeingData.minimumDamagePercent);
        }
        this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
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

