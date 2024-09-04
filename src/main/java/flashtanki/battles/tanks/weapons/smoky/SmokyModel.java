/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.tanks.weapons.smoky;

import flashtanki.utils.RandomUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.anticheats.AnticheatModel;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.IWeapon;
import flashtanki.battles.tanks.weapons.WeaponUtils;
import flashtanki.battles.tanks.weapons.WeaponWeakeningData;
import flashtanki.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import flashtanki.logger.LogType;
import flashtanki.logger.LoggerService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@AnticheatModel(name="SmokyModel", actionInfo="Child FireableWeaponAnticheatModel")
public class SmokyModel extends FireableWeaponAnticheatModel implements IWeapon {
    private static final LoggerService loggerService = LoggerService.getInstance();
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final SmokyEntity entity;
    private final WeaponWeakeningData weakeingData;

    public SmokyModel(SmokyEntity entity, WeaponWeakeningData weakeingData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
        this.weakeingData = weakeingData;
    }

    @Override
    public void fire(String json) {
        JSONParser js = new JSONParser();
        JSONObject jo = null;
        try {
            jo = (JSONObject)js.parse(json);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        if (!this.check((int)((Long)jo.get("reloadTime")).longValue())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
            return;
        }
        this.bfModel.fire(this.player, json);
        BattlefieldPlayerController victim = this.bfModel.players.get(jo.get("victimId"));
        if (victim == null) {
            return;
        }
        this.onTarget(new BattlefieldPlayerController[]{victim}, (int)Double.parseDouble(String.valueOf(jo.get("distance"))));
    }

    @Override
    public void startFire(String json) {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (targetsTanks.length == 0) {
            return;
        }
        if (targetsTanks.length > 1) {
            loggerService.log(LogType.INFO,"SmokyModel::onTarget() Warning! targetsTanks length = " + targetsTanks.length);
        }
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
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

