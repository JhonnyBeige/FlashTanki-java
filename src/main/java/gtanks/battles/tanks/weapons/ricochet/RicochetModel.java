/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.tanks.weapons.ricochet;

import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.ricochet.RicochetEntity;
import gtanks.commands.Type;
import gtanks.logger.LogType;
import gtanks.logger.LoggerService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@AnticheatModel(name="RicochetModel", actionInfo="Child FireableWeaponAnticheatModel")
public class RicochetModel
extends FireableWeaponAnticheatModel
implements IWeapon {
    private RicochetEntity entity;
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private final static LoggerService loggerService = LoggerService.getInstance();

    public RicochetModel(RicochetEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void fire(String json) {
        JSONObject parser = null;
        try {
            parser = (JSONObject)new JSONParser().parse(json);
        } catch (Exception var6) {
            var6.printStackTrace();
        }
        if (!this.check((int)((Long)parser.get("reloadTime")).longValue())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
        } else {
            BattlefieldPlayerController victim;
            boolean selfHit = parser.containsKey("self_hit") && (Boolean)parser.get("self_hit") != false;
            if (!selfHit) {
                this.bfModel.sendToAllPlayers(Type.BATTLE, "fire_ricochet", this.player.tank.id, json);
            }
            int distance = this.getValueByObject(parser.get("distance"));
            BattlefieldPlayerController battlefieldPlayerController = victim = selfHit ? this.player : this.bfModel.getPlayer((String)parser.get("victimId"));
            if (victim != null) {
                this.onTarget(new BattlefieldPlayerController[]{victim}, distance);
            }
        }
    }

    @Override
    public void startFire(String json) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire", this.player.tank.id, json);
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        BattlefieldPlayerController victim = targetsTanks[0];
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        this.bfModel.tanksKillModel.damageTank(victim, this.player, damage, true);
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    private int getValueByObject(Object obj) {
        if (obj == null) {
            return 0;
        }
        try {
            return (int)Double.parseDouble(String.valueOf(obj));
        }
        catch (Exception ex) {
            return Integer.parseInt(String.valueOf(obj));
        }
    }

    @Override
    public void stopFire() {
    }

    public void quickFire(String var1) {
    }
}

