/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.maps;

public class IMapConfigItem {
    public String id;
    public String name;
    public String skyboxId;
    public String ambientSoundId;
    public String gameMode;
    public String themeName;
    public int minRank;
    public int maxRank;
    public int maxPlayers;
    public boolean tdm = false;
    public boolean ctf = false;
    public boolean dom = false;

    public IMapConfigItem(String id, String name, String skyboxId, int minRank, int maxRank, int maxPlayers, boolean tdm, boolean ctf, boolean dom) {
        this.id = id;
        this.name = name;
        this.skyboxId = skyboxId;
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.tdm = tdm;
        this.ctf = ctf;
        this.dom = dom;
        this.maxPlayers = maxPlayers;
    }

    public IMapConfigItem(String id, String name, String skyboxId, int minRank, int maxRank, int maxPlayers, boolean tdm, boolean ctf, boolean dom, String soundId, String gamemodeId) {
        this.id = id;
        this.name = name;
        this.skyboxId = skyboxId;
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.tdm = tdm;
        this.ctf = ctf;
        this.dom = dom;
        this.maxPlayers = maxPlayers;
        this.ambientSoundId = soundId;
        this.gameMode = gamemodeId;
    }
}

