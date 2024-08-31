/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.users.garage.items;

import flashtanki.utils.StringUtils;
import flashtanki.system.localization.Localization;
import flashtanki.system.localization.strings.LocalizedString;
import flashtanki.users.garage.enums.ItemType;
import flashtanki.users.garage.items.modification.ModificationInfo;

public class Item {
    public String id;
    public LocalizedString description;
    public boolean isInventory;
    public int index;
    public PropertyItem[] propetys;
    public ItemType itemType;
    public int modificationIndex;
    public LocalizedString name;
    public PropertyItem[] nextProperty;
    public int nextPrice;
    public int nextRankId;
    public int price;
    public int rankId;
    public ModificationInfo[] modifications;
    public boolean specialItem;
    public int count;
    public int microUpgrades;
    public int microUpgradePrice;
    public long time;

    public Item(String id, LocalizedString description, boolean isInventory,
                int index, PropertyItem[] propetys, ItemType weapon,
                int modificationIndex, LocalizedString name, PropertyItem[] nextProperty,
                int nextPrice, int nextRankId, int price, int rankId,
                ModificationInfo[] modifications, boolean specialItem,
                int count, int microUpgrades, int microUpgradePrice, long time) {
        this.id = id;
        this.description = description;
        this.isInventory = isInventory;
        this.index = index;
        this.propetys = propetys;
        this.itemType = weapon;
        this.modificationIndex = modificationIndex;
        this.name = name;
        this.nextProperty = nextProperty;
        this.nextPrice = nextPrice;
        this.nextRankId = nextRankId;
        this.price = price;
        this.rankId = rankId;
        this.modifications = modifications;
        this.specialItem = specialItem;
        this.count = count;
        this.microUpgrades = microUpgrades;
        this.microUpgradePrice = microUpgradePrice;
        this.time = time;
    }

    public String getId() {
        return StringUtils.concatStrings(this.id, "_m", String.valueOf(this.modificationIndex));
    }

    public Item clone() {
        return new Item(this.id,
                this.description,
                this.isInventory,
                this.index,
                this.propetys,
                this.itemType,
                this.modificationIndex,
                this.name,
                this.nextProperty,
                this.nextPrice,
                this.nextRankId,
                this.price,
                this.rankId,
                this.modifications,
                this.specialItem,
                this.count,
                this.microUpgrades,
                this.microUpgradePrice,
                this.time);
    }
}