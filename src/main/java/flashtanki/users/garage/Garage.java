/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.users.garage;

import flashtanki.utils.StringUtils;
import flashtanki.commands.Type;
import flashtanki.json.JSONUtils;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.services.LobbysServices;
import flashtanki.battles.tanks.skin.SkinSystem;
import flashtanki.users.garage.enums.ItemType;
import flashtanki.users.garage.items.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import flashtanki.users.locations.UserLocation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Entity
@org.hibernate.annotations.Entity
@Table(name = "garages")
public class Garage implements Serializable {
    @Id
    @Column(name = "uid", nullable = false, unique = true)
    private long userId;
    @Column(name = "turrets", nullable = false)
    private String _json_turrets;
    @Column(name = "hulls", nullable = false)
    private String _json_hulls;
    @Column(name = "colormaps", nullable = false)
    private String _json_colormaps;
    @Column(name = "inventory", nullable = false)
    private String _json_inventory;
    @Column(name = "modules", nullable = false)
    private String _json_modules;
    @Transient
    public ArrayList<Item> items = new ArrayList();
    @Transient
    public Item mountTurret;
    @Transient
    public Item mountHull;
    @Transient
    public Item mountColormap;
    @Transient
    public Item mountModule;

    public Garage() {
    }

    public boolean containsItem(String id) {
        return items.stream()
                .anyMatch(item -> item.id.equals(id));
    }

    public Optional<Item> getItemById(String id) {
        return items.stream()
                .filter(item -> item.id.equals(id))
                .findFirst();
    }

    public void mountItem(String id) {
        Optional<Item> item = this.getItemById(id.substring(0, id.length() - 3));
        int index = Integer.parseInt(id.substring(id.length() - 1));

        if (item.isPresent()) {
            Item _item = item.get();
            if (index == _item.modificationIndex) {
                if (_item.itemType == ItemType.WEAPON) {
                    this.mountTurret = _item;
                }
                if (_item.itemType == ItemType.ARMOR) {
                    this.mountHull = _item;
                }
                if (_item.itemType == ItemType.COLOR) {
                    this.mountColormap = _item;
                }
                if(_item.itemType == ItemType.MODULE) {
                    this.mountModule = _item;
                }
                parseJSONData();
                DatabaseManagerImpl.instance().update(this);
                Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                        .ifPresent(lobbyManager ->
                                lobbyManager.send(Type.GARAGE, "mount_item",
                                        StringUtils.concatStrings(
                                                _item.id,
                                                "_m",
                                                String.valueOf(_item.modificationIndex)),
                                        SkinSystem.getInstance().getMountedSkinForUserAndItem(_item.id, userId)
                                                .orElse(StringUtils.concatStrings(
                                                        _item.id,
                                                        "_m",
                                                        String.valueOf(_item.modificationIndex))))

                        );
            }
        }
    }

    private void updateItem(String itemId, Runnable successCallback, Runnable failCallback) {
        String clientId = itemId.substring(0, itemId.length() - 3);
        Integer modification = Integer.parseInt(itemId.substring(itemId.length() - 1));
        this.getItemById(clientId)
                .filter(item -> item.modificationIndex < 3)
                .filter(item -> item.modificationIndex < modification)
                .ifPresentOrElse(item -> {
                            int previousModification = item.modificationIndex;
                            item.modificationIndex = modification;
                            int maxModificationIndex = 3;
                            item.nextPrice = item.modifications[Math.min(item.modificationIndex + 1, maxModificationIndex)].price;
                            item.nextProperty = item.modifications[Math.min(item.modificationIndex + 1, maxModificationIndex)].propertys;
                            item.nextRankId = item.modifications[Math.min(item.modificationIndex + 1, maxModificationIndex)].rank;
                            item.microUpgradePrice = 100;
                            item.microUpgrades = 0;
                            this.parseJSONData();
                            DatabaseManagerImpl.instance().update(this);
                            Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                                    .filter(lobbyManager -> lobbyManager.getLocalUser().getUserLocation()== UserLocation.GARAGE)
                                    .ifPresent(lobbyManager ->
                                            lobbyManager.send(Type.GARAGE, "update_item",
                                                    clientId+"_m"+previousModification,
                                                    clientId+"_m"+modification));
                            successCallback.run();
                        },
                        failCallback);
    }

    public void giveItem(String id, int count, Runnable successCallback, Runnable failCallback) {
        try{
            String itemClientId = id.substring(0, id.length() - 3);
            int modificationID = Integer.parseInt(id.substring(id.length() - 1));

            this.items.stream()
                .filter(item -> item.id.equals(itemClientId))
                .findFirst()
                .ifPresentOrElse(item -> {
                            giveItemIfUserAlreadyHaveIt(id, count, successCallback, failCallback, item, modificationID);
                        },
                        () -> {
                            giveItemIfUserDontHaveIt(successCallback, itemClientId, modificationID, count);
                        });
                        
        }catch(NumberFormatException e){
            e.printStackTrace();
            System.out.println(id);
        }
    }

    private void giveItemIfUserDontHaveIt(Runnable successCallback, String itemClientId, int modificationID, int count) {
        Item item = GarageItemsLoader.getInstance().items.get(itemClientId).clone();
        item.modificationIndex = modificationID;
        item.nextRankId = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].rank;
        item.nextPrice = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].price;
        item.microUpgradePrice = 100;
        item.microUpgrades = 0;
        item.count=count;
        this.items.add(item);
        parseJSONData();
        DatabaseManagerImpl.instance().update(this);
        Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                .filter(lobbyManager -> lobbyManager.getLocalUser().getUserLocation()== UserLocation.GARAGE
                        || lobbyManager.getLocalUser().getUserLocation()== UserLocation.ALL)
                .ifPresent(lobbyManager->
                        lobbyManager.send(Type.GARAGE, "buy_item",
                                StringUtils.concatStrings(item.id, "_m", String.valueOf(item.modificationIndex)),
                                JSONUtils.parseItemInfo(item)));
        successCallback.run();
    }

    private void giveItemIfUserAlreadyHaveIt(String id, int count, Runnable successCallback, Runnable failCallback, Item item, int modificationID) {
        if (item.itemType == ItemType.INVENTORY) {
            item.count += count;
            parseJSONData();
            DatabaseManagerImpl.instance().update(this);
            Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                    .filter(lobbyManager -> lobbyManager.getLocalUser().getUserLocation()== UserLocation.GARAGE
                            || lobbyManager.getLocalUser().getUserLocation()== UserLocation.ALL)
                    .ifPresent(lobbyManager->
                            lobbyManager.send(Type.GARAGE, "buy_item",
                                    StringUtils.concatStrings(item.id,
                                            "_m",
                                            String.valueOf(item.modificationIndex)),
                                    JSONUtils.parseItemInfo(item)));
            successCallback.run();
        } else if (item.modificationIndex < modificationID) {
            updateItem(id, successCallback, failCallback);
        }
    }


    public List<Item> getInventoryItems() {
        return this.items.stream()
                .filter(item -> item.itemType == ItemType.INVENTORY)
                .toList();
    }

    public void parseJSONData() {
        JSONObject hulls = new JSONObject();
        JSONArray _hulls = new JSONArray();
        JSONObject colormaps = new JSONObject();
        JSONArray _colormaps = new JSONArray();
        JSONObject turrets = new JSONObject();
        JSONArray _turrets = new JSONArray();
        JSONObject modules = new JSONObject();
        JSONArray _modules = new JSONArray();
        JSONObject inventory_items = new JSONObject();
        JSONArray _inventory = new JSONArray();
        for (Item item : this.items) {
            if (item.itemType == ItemType.ARMOR) {
                JSONObject hull = new JSONObject();
                hull.put("id", item.id);
                hull.put("modification", item.modificationIndex);
                hull.put("mounted", item == this.mountHull);
                hull.put("microUpgrades", item.microUpgrades);
                hull.put("microUpgradePrice", item.microUpgradePrice);
                _hulls.add(hull);
            }
            if (item.itemType == ItemType.COLOR) {
                JSONObject colormap = new JSONObject();
                colormap.put("id", item.id);
                colormap.put("modification", item.modificationIndex);
                colormap.put("mounted", item == this.mountColormap);
                _colormaps.add(colormap);
            }
            if (item.itemType == ItemType.WEAPON) {
                JSONObject turret = new JSONObject();
                turret.put("id", item.id);
                turret.put("modification", item.modificationIndex);
                turret.put("mounted", item == this.mountTurret);
                turret.put("microUpgrades", item.microUpgrades);
                turret.put("microUpgradePrice", item.microUpgradePrice);
                _turrets.add(turret);
            }
            if(item.itemType == ItemType.MODULE) {
                JSONObject module = new JSONObject();
                module.put("id", item.id);
                module.put("modification", item.modificationIndex);
                module.put("mounted", item == this.mountModule);
                _modules.add(module);
            }
            if (item.itemType != ItemType.INVENTORY) continue;
            JSONObject inventory = new JSONObject();
            inventory.put("id", item.id);
            inventory.put("count", item.count);
            _inventory.add(inventory);
        }
        hulls.put("hulls", _hulls);
        colormaps.put("colormaps", _colormaps);
        turrets.put("turrets", _turrets);
        modules.put("modules", _modules);

        inventory_items.put("inventory", _inventory);
        this._json_colormaps = colormaps.toJSONString();
        this._json_hulls = hulls.toJSONString();
        this._json_turrets = turrets.toJSONString();
        this._json_inventory = inventory_items.toJSONString();
        this._json_modules = modules.toJSONString();
    }

    public void unparseJSONData() throws ParseException {
        Item item;
        this.items.clear();
        JSONParser parser = new JSONParser();
        JSONObject turrets = (JSONObject) parser.parse(this._json_turrets);
        JSONObject colormaps = (JSONObject) parser.parse(this._json_colormaps);
        JSONObject hulls = (JSONObject) parser.parse(this._json_hulls);
        JSONObject modules = (JSONObject) parser.parse(this._json_modules);
        JSONObject inventory = this._json_inventory == null || this._json_inventory.isEmpty() ? null : (JSONObject) parser.parse(this._json_inventory);
        for (Object _turret : (JSONArray) turrets.get("turrets")) {
            JSONObject turret = (JSONObject) _turret;
            item = GarageItemsLoader.getInstance().items.get(turret.get("id")).clone();
            item.modificationIndex = (int) ((Long) turret.get("modification")).longValue();
            item.nextRankId = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].rank;
            item.nextPrice = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].price;
            item.microUpgrades = turret.get("microUpgrades") != null ? (int) ((Long) turret.get("microUpgrades")).longValue() : 0;
            item.microUpgradePrice = turret.get("microUpgradePrice") != null ? (int) ((Long) turret.get("microUpgradePrice")).longValue() : 100;
            this.items.add(item);
            if (!((Boolean) turret.get("mounted")).booleanValue()) continue;
            this.mountTurret = item;
        }
        for (Object _colormap : (JSONArray) colormaps.get("colormaps")) {
            JSONObject colormap = (JSONObject) _colormap;
            if(colormap.get("id") != null && GarageItemsLoader.getInstance().items.get(colormap.get("id")) != null){
                item = GarageItemsLoader.getInstance().items.get(colormap.get("id")).clone();
                item.modificationIndex = (int) ((Long) colormap.get("modification")).longValue();
                this.items.add(item);
                if (!((Boolean) colormap.get("mounted")).booleanValue()) continue;
                this.mountColormap = item;
            }
        }
        for(Object _module : (JSONArray) modules.get("modules")) {
            JSONObject module = (JSONObject) _module;
            item = GarageItemsLoader.getInstance().items.get(module.get("id")).clone();
            item.modificationIndex = (int) ((Long) module.get("modification")).longValue();
            this.items.add(item);
            if (!((Boolean) module.get("mounted")).booleanValue()) continue;
            this.mountModule = item;
        }
        for (Object _hull : (JSONArray) hulls.get("hulls")) {
            JSONObject hull = (JSONObject) _hull;
            item = GarageItemsLoader.getInstance().items.get(hull.get("id")).clone();
            item.modificationIndex = (int) ((Long) hull.get("modification")).longValue();
            item.nextRankId = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].rank;
            item.nextPrice = item.modifications[item.modificationIndex == 3 ? 3 : item.modificationIndex + 1].price;
            item.microUpgrades = hull.get("microUpgrades") != null ? (int) ((Long) hull.get("microUpgrades")).longValue() : 0;
            item.microUpgradePrice = hull.get("microUpgradePrice") != null ? (int) ((Long) hull.get("microUpgradePrice")).longValue() : 100;
            this.items.add(item);
            if (!((Boolean) hull.get("mounted")).booleanValue()) continue;
            this.mountHull = item;
        }
        if (inventory != null) {
            for (Object inventory_item : (JSONArray) inventory.get("inventory")) {
                JSONObject _item = (JSONObject) inventory_item;
                item = GarageItemsLoader.getInstance().items.get(_item.get("id")).clone();
                item.modificationIndex = 0;
                item.count = (int) ((Long) _item.get("count")).longValue();
                if (item.itemType != ItemType.INVENTORY) continue;
                this.items.add(item);
            }
        }
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void addItem(Item reward) {
        for (Item item : items) {
            if (item.getId().equals(reward.getId())) {
                item.incrementQuantity(reward.getQuantity());
                return;
            }
        }

        items.add(reward);
    }
}

