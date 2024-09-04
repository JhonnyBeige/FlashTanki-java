/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.system.localization.strings;

import flashtanki.system.localization.Localization;
import java.util.HashMap;
import java.util.Map;

public class LocalizedString {
    private final Map<Localization, String> localizatedMap = new HashMap<Localization, String>();

    protected LocalizedString(String ruVersion, String enVersion) {
        this.localizatedMap.put(Localization.RU, ruVersion);
        this.localizatedMap.put(Localization.EN, enVersion);
    }

    public String localizatedString(Localization loc) {
        String string = this.localizatedMap.get(loc);
        return string == null ? "null" : string;
    }
}

