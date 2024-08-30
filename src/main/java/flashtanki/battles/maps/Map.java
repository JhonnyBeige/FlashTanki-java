/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.maps;

import flashtanki.battles.bonuses.BonusRegion;
import flashtanki.battles.maps.parser.map.keypoints.DOMKeypoint;
import flashtanki.battles.maps.themes.MapTheme;
import flashtanki.battles.tanks.math.Vector3;
import java.util.ArrayList;
import java.util.List;

public class Map {
    public String name;
    public String id;
    public String skyboxId;
    public String themeId;
    public MapTheme mapTheme;
    public int minRank;
    public int maxRank;
    public int maxPlayers;
    public boolean tdm = false;
    public boolean ctf = false;
    public boolean dom = false;
    public ArrayList<Vector3> spawnPositonsDM = new ArrayList();
    public ArrayList<Vector3> spawnPositonsBlue = new ArrayList();
    public ArrayList<Vector3> spawnPositonsRed = new ArrayList();
    public ArrayList<BonusRegion> goldsRegions = new ArrayList();
    public ArrayList<BonusRegion> crystallsRegions = new ArrayList();
    public ArrayList<BonusRegion> healthsRegions = new ArrayList();
    public ArrayList<BonusRegion> armorsRegions = new ArrayList();
    public ArrayList<BonusRegion> damagesRegions = new ArrayList();
    public ArrayList<BonusRegion> nitrosRegions = new ArrayList();
    public List<DOMKeypoint> domKeypoints = new ArrayList<DOMKeypoint>();
    public int totalCountDrops;
    public Vector3 flagRedPosition;
    public Vector3 flagBluePosition;
    public String md5Hash;

    public Map() {
    }

    public Map(String name, String id, String skyboxId, String themeId, MapTheme mapTheme, int minRank, int maxRank, int maxPlayers, boolean tdm, boolean ctf, boolean dom, String md5Hash) {
        this.name = name;
        this.id = id;
        this.skyboxId = skyboxId;
        this.themeId = themeId;
        this.mapTheme = mapTheme;
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.maxPlayers = maxPlayers;
        this.tdm = tdm;
        this.ctf = ctf;
        this.dom = dom;
        this.md5Hash = md5Hash;
    }
}

