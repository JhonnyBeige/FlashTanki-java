package flashtanki.battles.tanks.module;

import flashtanki.users.garage.enums.PropertyType;

import java.util.HashMap;
import java.util.Map;

public class ModuleFactory {
    private static ModuleFactory instance;
    public static ModuleFactory getInstance() {
        if (instance == null) {
            instance = new ModuleFactory();
        }
        return instance;
    }
    private ModuleFactory() {
    }
    private final Map<String, Module> modules = new HashMap<String, Module>();

    public  void addColormap(String id, Module colormap) {
        modules.put(id, colormap);
    }

    public  Module getModule(String id) {
        return modules.get(id);
    }

    public  ModuleResistanceType getResistanceType(PropertyType pType) {
        ModuleResistanceType type = null;
        switch (pType) {
            case FIRE_RESISTANCE: {
                type = ModuleResistanceType.FLAMETHROWER;
                break;
            }
            case FREEZE_RESISTANCE: {
                type = ModuleResistanceType.FREZEE;
                break;
            }
            case MECH_RESISTANCE: {
                type = ModuleResistanceType.SMOKY;
                break;
            }
            case PLASMA_RESISTANCE: {
                type = ModuleResistanceType.TWINS;
                break;
            }
            case RAIL_RESISTANCE: {
                type = ModuleResistanceType.RAILGUN;
                break;
            }
            case RICOCHET_RESISTANCE: {
                type = ModuleResistanceType.RICOCHET;
                break;
            }
            case THUNDER_RESISTANCE: {
                type = ModuleResistanceType.THUNDER;
                break;
            }
            case VAMPIRE_RESISTANCE: {
                type = ModuleResistanceType.ISIDA;
                break;
            }
            case SHAFT_RESISTANCE: {
                type = ModuleResistanceType.SHAFT;
                break;
            }
            case SHAFT_DAMAGE: {
                break;
            }
            case SHAFT_SHOT_FREQUENCY: {
                break;
            }
        }
        return type;
    }
}
