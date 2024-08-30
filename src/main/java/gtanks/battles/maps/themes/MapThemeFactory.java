/*
 * Decompiled with CFR 0.150.
 */
package gtanks.battles.maps.themes;

import gtanks.battles.maps.themes.MapTheme;

public class MapThemeFactory {
    public static MapTheme getDefaultMapTheme() {
        return new MapTheme("default","default_ambient_sound");
    }
    public static MapTheme getMapTheme(String soundId, String gameMode) {
        return new MapTheme(soundId, gameMode);
    }
}

