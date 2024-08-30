/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.users.garage.items;

import flashtanki.users.garage.enums.PropertyType;

public class PropertyItem {
    public PropertyType property;
    public String value;

    public PropertyItem(PropertyType property, String value) {
        this.property = property;
        this.value = value;
    }
}

