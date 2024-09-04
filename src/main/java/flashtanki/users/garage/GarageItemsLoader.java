/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.users.garage;

import flashtanki.battles.tanks.module.Module;
import flashtanki.battles.tanks.module.ModuleFactory;
import flashtanki.system.localization.strings.LocalizedString;
import flashtanki.system.localization.strings.StringsLocalizationBundle;
import flashtanki.users.garage.enums.ItemType;
import flashtanki.users.garage.enums.PropertyType;
import flashtanki.users.garage.items.Item;
import flashtanki.users.garage.items.PropertyItem;
import flashtanki.users.garage.items.modification.ModificationInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GarageItemsLoader {

    public HashMap<String, Item> items;
    private final ModuleFactory moduleFactory = ModuleFactory.getInstance();
    private int index;
    private static GarageItemsLoader instance;

    {
        index = 1;
    }

    public static GarageItemsLoader getInstance() {
        if (instance == null) {
            instance = new GarageItemsLoader();
        }
        return instance;
    }

    public void loadFromConfig(String turrets, String hulls, String colormaps, String inventory, String moduules) {
        if (items == null) {
            items = new HashMap();
        }
        for (int i = 0; i < 5; ++i) {
            StringBuilder builder = new StringBuilder();
            try {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(
                                i == 0 ? inventory :
                                        (i == 1 ? turrets :
                                                (i == 2 ? hulls :
                                                        (i == 3 ? colormaps : moduules)))),
                        StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            parseAndInitItems(builder.toString(), i == 0 ? ItemType.INVENTORY
                    : (i == 1 ? ItemType.WEAPON : (i == 2 ? ItemType.ARMOR :
                    (i == 3 ? ItemType.COLOR : ItemType.MODULE))));
        }
    }

    private void parseAndInitItems(String json, ItemType typeItem) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(json);
            JSONObject jparser = (JSONObject) obj;
            JSONArray jarray = (JSONArray) jparser.get("items");
            for (int i = 0; i < jarray.size(); ++i) {
                JSONObject item = (JSONObject) jarray.get(i);
                LocalizedString name = StringsLocalizationBundle.registerString((String) item.get("name_ru"),
                        (String) item.get("name_en"));
                LocalizedString description = StringsLocalizationBundle
                        .registerString((String) item.get("description_ru"), (String) item.get("description_en"));
                String id = (String) item.get("id");

                int priceM0 = Integer.parseInt((String) item.get("price_m0"));
                int priceM1 = typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY
                        || typeItem == ItemType.PLUGIN || typeItem == ItemType.MODULE ? priceM0 : Integer.parseInt((String) item.get("price_m1"));
                int priceM2 = typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY
                        || typeItem == ItemType.PLUGIN || typeItem == ItemType.MODULE ? priceM0 : Integer.parseInt((String) item.get("price_m2"));
                int priceM3 = typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY
                        || typeItem == ItemType.PLUGIN || typeItem == ItemType.MODULE ? priceM0 : Integer.parseInt((String) item.get("price_m3"));
                int rangM0 = Integer.parseInt((String) item.get("rang_m0"));
                int rangM1 = typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN || typeItem == ItemType.MODULE
                        ? rangM0
                        : Integer.parseInt((String) item.get("rang_m1"));
                int rangM2 = typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN || typeItem == ItemType.MODULE
                        ? rangM0
                        : Integer.parseInt((String) item.get("rang_m2"));
                int rangM3 = typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN || typeItem == ItemType.MODULE
                        ? rangM0
                        : Integer.parseInt((String) item.get("rang_m3"));
                PropertyItem[] propertysItemM0 = null;
                PropertyItem[] propertysItemM1 = null;
                PropertyItem[] propertysItemM2 = null;
                PropertyItem[] propertysItemM3 = null;
                int countModification = typeItem == ItemType.COLOR || typeItem == ItemType.MODULE ? 1
                        : (typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN
                        ? (int) ((Long) item.get("count_modifications")).longValue()
                        : 4);
                block9:
                for (int m = 0; m < countModification; ++m) {
                    JSONArray propertys = (JSONArray) item.get("propertys_m" + m);
                    PropertyItem[] property = new PropertyItem[propertys.size()];
                    for (int p = 0; p < propertys.size(); ++p) {
                        JSONObject prop = (JSONObject) propertys.get(p);
                        String type = (String) prop.get("type");
                        String value = (String) prop.get("value");
                        property[p] = new PropertyItem(getType(type),
                                value);
                    }
                    switch (m) {
                        case 0: {
                            propertysItemM0 = property;
                            continue block9;
                        }
                        case 1: {
                            propertysItemM1 = property;
                            continue block9;
                        }
                        case 2: {
                            propertysItemM2 = property;
                            continue block9;
                        }
                        case 3: {
                            propertysItemM3 = property;
                        }
                    }
                }
                if (typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN || typeItem == ItemType.MODULE) {
                    propertysItemM1 = propertysItemM0;
                    propertysItemM2 = propertysItemM0;
                    propertysItemM3 = propertysItemM0;
                }
                ModificationInfo[] mods = new ModificationInfo[4];
                mods[0] = new ModificationInfo(id + "_m0", priceM0, rangM0);
                mods[0].propertys = propertysItemM0;
                mods[1] = new ModificationInfo(id + "_m1", priceM1, rangM1);
                mods[1].propertys = propertysItemM1;
                mods[2] = new ModificationInfo(id + "_m2", priceM2, rangM2);
                mods[2].propertys = propertysItemM2;
                mods[3] = new ModificationInfo(id + "_m3", priceM3, rangM3);
                mods[3].propertys = propertysItemM3;
                boolean specialItem = item.get("special_item") != null && (Boolean) item.get("special_item");
                int microUpgrades = item.get("microUpgrades") != null ? Integer.parseInt((String) item.get("microUpgrades")) : 0;
                int microUpgradePrice = item.get("microUpgradePrice") != null ? Integer.parseInt((String) item.get("microUpgradePrice")) : 100;
                long time = 0L;
                if (id == "premium") {
                    time = ((Long) item.get("premium_time")).longValue();
                }
                items.put(id,
                        new Item(id, description, typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN, index,
                                propertysItemM0, typeItem, 0, name, propertysItemM1, priceM1, rangM1, priceM0, rangM0,
                                mods, specialItem, 0, microUpgrades, microUpgradePrice, time));
                ++index;
                if (typeItem != ItemType.MODULE)
                    continue;
                Module module = new Module();
                // FIXME check correctly variable mods
                PropertyItem[] arrpropertyItem = mods[0].propertys;
                int n = mods[0].propertys.length;
                for (int j = 0; j < n; ++j) {
                    PropertyItem _property = arrpropertyItem[j];
                    module.addResistance(moduleFactory.getResistanceType(_property.property),
                            getInt(_property.value.replace("%", "")));
                }
                moduleFactory.addColormap(id + "_m0", module);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private int getInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) {
            return 0;
        }
    }

    private PropertyType getType(String s) {
        for (PropertyType type : PropertyType.values()) {
            if (!type.toString().equals(s))
                continue;
            return type;
        }
        return null;
    }
}
