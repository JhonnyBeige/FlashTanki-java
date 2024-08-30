/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.tanks.loaders;

import gtanks.StringUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.ShotData;
import gtanks.battles.tanks.weapons.WeaponDamageData;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerEntity;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerModel;
import gtanks.battles.tanks.weapons.frezee.FrezeeEntity;
import gtanks.battles.tanks.weapons.frezee.FrezeeModel;
import gtanks.battles.tanks.weapons.isida.IsidaEntity;
import gtanks.battles.tanks.weapons.isida.IsidaModel;
import gtanks.battles.tanks.weapons.railgun.RailgunEntity;
import gtanks.battles.tanks.weapons.railgun.RailgunModel;
import gtanks.battles.tanks.weapons.ricochet.RicochetEntity;
import gtanks.battles.tanks.weapons.ricochet.RicochetModel;
import gtanks.battles.tanks.weapons.shaft.ShaftEntity;
import gtanks.battles.tanks.weapons.shaft.ShaftModel;
import gtanks.battles.tanks.weapons.smoky.SmokyEntity;
import gtanks.battles.tanks.weapons.smoky.SmokyModel;
import gtanks.battles.tanks.weapons.snowman.SnowmanEntity;
import gtanks.battles.tanks.weapons.snowman.SnowmanModel;
import gtanks.battles.tanks.weapons.thunder.ThunderEntity;
import gtanks.battles.tanks.weapons.thunder.ThunderModel;
import gtanks.battles.tanks.weapons.twins.TwinsEntity;
import gtanks.battles.tanks.weapons.twins.TwinsModel;
import gtanks.json.JSONUtils;
import gtanks.logger.LogType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import gtanks.logger.LoggerService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WeaponsFactory {
    private HashMap<String, IEntity> weapons = new HashMap();
    private HashMap<String, WeaponWeakeningData> wwd = new HashMap();
    private String jsonListWeapons;
    private String sfxData = SFXDataFactory.getData();
    private static final LoggerService loggerService = LoggerService.getInstance();
    private static WeaponsFactory instance;
    private static HashMap<String, WeaponDamageData> weaponsDamageData = new HashMap<>();

    public static WeaponsFactory getInstance() {
        if (instance == null) {
            instance = new WeaponsFactory();
        }
        return instance;
    }


    public IWeapon getWeapon(String turretId, BattlefieldPlayerController tank, BattlefieldModel battle) {
        String turret = turretId.split("_m")[0];
        IWeapon weapon = null;
        switch (turret) {
            case "railgun": {
                weapon = new RailgunModel((RailgunEntity) getEntity(turretId), tank, battle);
                break;
            }
            case "smoky": {
                weapon = new SmokyModel((SmokyEntity) getEntity(turretId), getWwd(turretId), battle, tank);
                break;
            }
            case "flamethrower": {
                weapon = new FlamethrowerModel((FlamethrowerEntity) getEntity(turretId), battle, tank);
                break;
            }
            case "twins": {
                weapon = new TwinsModel((TwinsEntity) getEntity(turretId), getWwd(turretId), tank, battle);
                break;
            }
            case "isida": {
                weapon = new IsidaModel((IsidaEntity) getEntity(turretId), tank, battle);
                break;
            }
            case "thunder": {
                weapon = new ThunderModel((ThunderEntity) getEntity(turretId), battle, tank);
                break;
            }
            case "frezee": {
                weapon = new FrezeeModel((FrezeeEntity) getEntity(turretId), battle, tank);
                break;
            }
            case "ricochet": {
                weapon = new RicochetModel((RicochetEntity) getEntity(turretId), battle, tank);
                break;
            }
            case "shaft": {
                weapon = new ShaftModel((ShaftEntity) getEntity(turretId), getWwd(turretId), battle, tank);
                break;
            }
            case "snowman": {
                weapon = new SnowmanModel((SnowmanEntity) getEntity(turretId), getWwd(turretId), tank, battle);
                break;
            }
            default: {
                weapon = new RailgunModel((RailgunEntity) getEntity("railgun_m0"), tank, battle);
            }
        }
        return weapon;
    }

    public void init(String path2config) {
        weapons.clear();
        loggerService.log(LogType.INFO, "Weapons Factory inited. Loading weapons...");
        try {
            File folder = new File(path2config);
            for (File config : folder.listFiles()) {
                if (!config.getName().endsWith(".cfg")) {
                    String errMsg = "In folder " + path2config + " find non-configuration file: " + config.getName();
                    loggerService.log(LogType.ERROR, errMsg);
                    throw new Exception(errMsg);
                }
                loggerService.log(LogType.INFO, "Loading " + config.getName() + "...");
                parse(config);
            }
            jsonListWeapons = JSONUtils.parseWeapons(getEntitys(), wwd);
        } catch (Exception ex) {
            ex.printStackTrace();
            loggerService.log(LogType.ERROR, "Loading entitys weapons failed. " + ex.getMessage());
        }
    }

    private void parse(File json) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jobj = (JSONObject) parser.parse(new FileReader(json));
        String type = (String) jobj.get("type");
        for (Object item : (JSONArray) jobj.get("params")) {
            JSONObject jitem = (JSONObject) item;
            String modification = (String) jitem.get("modification");
            String id = StringUtils.concatStrings(type, "_", modification);
            String justId = StringUtils.concatStrings(type, (String) modification.split("m")[1]);
            ShotData shotData = new ShotData(id, getDouble(jitem.get("autoAimingAngleDown")), getDouble(jitem.get("autoAimingAngleUp")), (int) ((Long) jitem.get("numRaysDown")).longValue(), (int) ((Long) jitem.get("numRaysUp")).longValue(), (int) ((Long) jitem.get("reloadMsec")).longValue(), (float) ((Double) jitem.get("impactCoeff")).doubleValue(), (float) ((Double) jitem.get("kickback")).doubleValue(), (float) ((Double) jitem.get("turretRotationAccel")).doubleValue(), (float) ((Double) jitem.get("turretRotationSpeed")).doubleValue());
            IEntity entity = null;
            weaponsDamageData.put((String) id, new WeaponDamageData((float) ((Double) jitem.get("max_damage")).doubleValue()));
            weaponsDamageData.put((String) justId, new WeaponDamageData((float) ((Double) jitem.get("max_damage")).doubleValue()));
            switch (type) {
                case "railgun": {
                    entity = new RailgunEntity(shotData, (int) ((Long) jitem.get("charingTime")).longValue(), (int) ((Long) jitem.get("weakeningCoeff")).longValue(), (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue());
                    break;
                }
                case "smoky": {
                    WeaponWeakeningData wwd = new WeaponWeakeningData((Double) jitem.get("max_damage_radius"), (Double) jitem.get("min_damage_percent"), (Double) jitem.get("min_damage_radius"));
                    entity = new SmokyEntity(shotData, (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue());
                    this.wwd.put(id, wwd);
                    break;
                }
                case "flamethrower": {
                    entity = new FlamethrowerEntity((int) ((Long) jitem.get("target_detection_interval")).longValue(), (float) ((Double) jitem.get("range")).doubleValue(), (float) ((Double) jitem.get("cone_angle")).doubleValue(), (int) ((Long) jitem.get("heating_speed")).longValue(), (int) ((Long) jitem.get("cooling_speed")).longValue(), (int) ((Long) jitem.get("heat_limit")).longValue(), shotData, (float) ((Double) jitem.get("max_damage")).doubleValue(), (float) ((Double) jitem.get("min_damage")).doubleValue());
                    break;
                }
                case "twins": {
                    WeaponWeakeningData wwdTwins = new WeaponWeakeningData((Double) jitem.get("max_damage_radius"), (Double) jitem.get("min_damage_percent"), (Double) jitem.get("min_damage_radius"));
                    entity = new TwinsEntity((float) ((Double) jitem.get("shot_range")).doubleValue(), (float) ((Double) jitem.get("shot_speed")).doubleValue(), (float) ((Double) jitem.get("shot_radius")).doubleValue(), (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue(), shotData);
                    wwd.put(id, wwdTwins);
                    break;
                }
                case "isida": {
                    entity = new IsidaEntity((int) ((Long) jitem.get("capacity")).longValue(), (int) ((Long) jitem.get("chargeRate")).longValue(), (int) ((Long) jitem.get("dischargeRate")).longValue(), (int) ((Long) jitem.get("tickPeriod")).longValue(), (float) ((Double) jitem.get("lockAngle")).doubleValue(), (float) ((Double) jitem.get("lockAngleCos")).doubleValue(), (float) ((Double) jitem.get("maxAngle")).doubleValue(), (float) ((Double) jitem.get("maxAngleCos")).doubleValue(), (float) ((Double) jitem.get("maxRadius")).doubleValue(), shotData, (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue());
                    break;
                }
                case "thunder": {
                    WeaponWeakeningData wwdThunder = new WeaponWeakeningData((Double) jitem.get("maxSplashDamageRadius"), (Double) jitem.get("minSplashDamageRadius"), (Double) jitem.get("minSplashDamagePercent"));
                    entity = new ThunderEntity((float) ((Double) jitem.get("maxSplashDamageRadius")).doubleValue(), (float) ((Double) jitem.get("minSplashDamageRadius")).doubleValue(), (float) ((Double) jitem.get("minSplashDamagePercent")).doubleValue(), (float) ((Double) jitem.get("impactForce")).doubleValue(), shotData, (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue(), wwdThunder);
                    wwd.put(id, wwdThunder);
                    break;
                }
                case "frezee": {
                    entity = new FrezeeEntity((float) ((Double) jitem.get("damageAreaConeAngle")).doubleValue(), (float) ((Double) jitem.get("damageAreaRange")).doubleValue(), (int) ((Long) jitem.get("energyCapacity")).longValue(), (int) ((Long) jitem.get("energyDischargeSpeed")).longValue(), (int) ((Long) jitem.get("energyRechargeSpeed")).longValue(), (int) ((Long) jitem.get("weaponTickMsec")).longValue(), (float) ((Double) jitem.get("coolingSpeed")).doubleValue(), (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue(), shotData);
                    break;
                }
                case "ricochet": {
                    WeaponWeakeningData wwdRicochet = new WeaponWeakeningData((Double) jitem.get("max_damage_radius"), (Double) jitem.get("min_damage_percent"), (Double) jitem.get("min_damage_radius"));
                    entity = new RicochetEntity((float) ((Double) jitem.get("shotRadius")).doubleValue(), (float) ((Double) jitem.get("shotSpeed")).doubleValue(), (int) ((Long) jitem.get("energyCapacity")).longValue(), (int) ((Long) jitem.get("energyPerShot")).longValue(), (float) ((Double) jitem.get("energyRechargeSpeed")).doubleValue(), (float) ((Double) jitem.get("shotDistance")).doubleValue(), (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue(), shotData);
                    wwd.put(id, wwdRicochet);
                    break;
                }
                case "shaft": {
                    WeaponWeakeningData wwdShaft = new WeaponWeakeningData((Double)jitem.get("max_damage_radius"), (Double)jitem.get("min_damage_percent"), (Double)jitem.get("min_damage_radius"));
                    entity = new ShaftEntity((float) ((Double)jitem.get("min_damage")).doubleValue(), (float) ((Double)jitem.get("max_damage")).doubleValue(), (float) ((Double)jitem.get("fov_max_damage")).doubleValue(), (float) ((Double)jitem.get("max_energy")).doubleValue(), (float) ((Double)jitem.get("charge_rate")).doubleValue(), (float) ((Double)jitem.get("discharge_rate")).doubleValue(), (float) ((Double)jitem.get("elevation_angle_up")).doubleValue(), (float) ((Double)jitem.get("elevation_angle_down")).doubleValue(), (float) ((Double)jitem.get("vertical_targeting_speed")).doubleValue(), (float) ((Double)jitem.get("horizontal_targeting_speed")).doubleValue(), (float) ((Double)jitem.get("inital_fov")).doubleValue(), (float) ((Double)jitem.get("minimum_fov")).doubleValue(), (float) ((Double)jitem.get("shrubs_hiding_radius_min")).doubleValue(), (float) ((Double)jitem.get("shrubs_hiding_radius_max")).doubleValue(), (float) ((Double)jitem.get("impact_quick_shot")).doubleValue(), shotData);
                    wwd.put(id, wwdShaft);
                    break;
                }
                case "snowman": {
                    WeaponWeakeningData wwdSnowman = new WeaponWeakeningData((Double) jitem.get("max_damage_radius"), (Double) jitem.get("min_damage_percent"), (Double) jitem.get("min_damage_radius"));
                    entity = new SnowmanEntity((float) ((Double) jitem.get("shot_range")).doubleValue(), (float) ((Double) jitem.get("shot_speed")).doubleValue(), (float) ((Double) jitem.get("shot_radius")).doubleValue(), (float) ((Double) jitem.get("min_damage")).doubleValue(), (float) ((Double) jitem.get("max_damage")).doubleValue(), (float) ((Double) jitem.get("frezee_speed")).doubleValue(), shotData);
                    wwd.put(id, wwdSnowman);
                }
            }
            weapons.put(id, entity);
        }
    }

    public WeaponWeakeningData getWwd(String id) {
        return wwd.get(id);
    }

    public IEntity getEntity(String id) {
        return weapons.get(id);
    }

    public WeaponDamageData getWeaponDamageData(String id) {
        return weaponsDamageData.get(id);
    }

    public String getId(IEntity entity) {
        String id = null;
        for (Map.Entry<String, IEntity> entry : weapons.entrySet()) {
            if (!entry.getValue().equals(entity)) continue;
            id = entry.getKey();
        }
        return id;
    }

    private double getDouble(Object obj) {
        try {
            return (Double) obj;
        } catch (Exception ex) {
            return ((Long) obj).longValue();
        }
    }

    public Collection<IEntity> getEntitys() {
        return weapons.values();
    }

    public String getJSONList() {
        return jsonListWeapons;
    }

    public String getSFXData() {
        return sfxData;
    }
}

