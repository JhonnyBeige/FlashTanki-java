package flashtanki.battles.tanks.module;

import flashtanki.battles.tanks.weapons.EntityType;

import java.util.HashMap;

public class Module {
    private HashMap<ModuleResistanceType, Integer> resistances = new HashMap();

    public void addResistance(ModuleResistanceType type, int percent) {
        this.resistances.put(type, percent);
    }

    public Integer getResistance(EntityType weaponType) {
        return this.resistances.getOrDefault((Object)this.getResistanceTypeByWeapon(weaponType),0);
    }

    private ModuleResistanceType getResistanceTypeByWeapon(EntityType weaponType) {
        ModuleResistanceType type = null;
        switch (weaponType) {
            case SMOKY: {
                type = ModuleResistanceType.SMOKY;
                break;
            }
            case FLAMETHROWER: {
                type = ModuleResistanceType.FLAMETHROWER;
                break;
            }
            case FREZZE: {
                type = ModuleResistanceType.FREZEE;
                break;
            }
            case ISIDA: {
                type = ModuleResistanceType.ISIDA;
                break;
            }
            case RAILGUN: {
                type = ModuleResistanceType.RAILGUN;
                break;
            }
            case RICOCHET: {
                type = ModuleResistanceType.RICOCHET;
                break;
            }
            case THUNDER: {
                type = ModuleResistanceType.THUNDER;
                break;
            }
            case TWINS: {
                type = ModuleResistanceType.TWINS;
            }
            case SHAFT: {
                type = ModuleResistanceType.SHAFT;
            }
        }
        return type;
    }

}
