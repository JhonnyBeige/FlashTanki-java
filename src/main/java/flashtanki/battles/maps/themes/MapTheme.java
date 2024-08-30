/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.maps.themes;

public class MapTheme {
    public MapTheme(String gameModeId, String ambientSoundId) {
        this.gameModeId = gameModeId;
        this.ambientSoundId = ambientSoundId;
    }

    private String gameModeId;
    private String ambientSoundId;

    public String getAmbientSoundId() {
        return this.ambientSoundId;
    }

    protected void setAmbientSoundId(String ambientSoundId) {
        this.ambientSoundId = ambientSoundId;
    }

    public String getGameModeId() {
        return this.gameModeId;
    }

    protected void setGameModeId(String gameModeId) {
        this.gameModeId = gameModeId;
    }
}

