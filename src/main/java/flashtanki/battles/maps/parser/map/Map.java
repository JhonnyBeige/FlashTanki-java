/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlRootElement
 */
package flashtanki.battles.maps.parser.map;

import flashtanki.battles.maps.parser.Vector3d;
import flashtanki.battles.maps.parser.map.bonus.BonusRegion;
import flashtanki.battles.maps.parser.map.keypoints.DOMKeypoint;
import flashtanki.battles.maps.parser.map.spawn.SpawnPosition;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="map")
public class Map {
    private SpawnPoints spawnPoints;
    private BonusRegions bonusRegions;
    private FlagsPositions flagPositions;
    private DOMKeypoints points;

    public SpawnPoints getSpawnPoints() {
        return this.spawnPoints;
    }

    @XmlElement(name="spawn-points")
    public void setSpawnPoints(SpawnPoints spawnPoints) {
        this.spawnPoints = spawnPoints;
    }

    public BonusRegions getBonusRegions() {
        return this.bonusRegions;
    }

    @XmlElement(name="bonus-regions")
    public void setBonusRegions(BonusRegions bonusRegions) {
        this.bonusRegions = bonusRegions;
    }

    public FlagsPositions getFlagPositions() {
        return this.flagPositions;
    }

    @XmlElement(name="ctf-flags")
    public void setFlagPositions(FlagsPositions flagPositions) {
        this.flagPositions = flagPositions;
    }

    public Vector3d getPositionBlueFlag() {
        return this.getFlagPositions() != null ? this.getFlagPositions().getBlueFlag() : null;
    }

    public Vector3d getPositionRedFlag() {
        return this.getFlagPositions() != null ? this.getFlagPositions().getRedFlag() : null;
    }

    public List<SpawnPosition> getSpawnPositions() {
        return this.spawnPoints.getSpawnPositions();
    }

    public List<BonusRegion> getBonusesRegion() {
        return this.bonusRegions.getBonusRegions();
    }

    @XmlElement(name="dom-keypoints")
    public DOMKeypoints getPoints() {
        return this.points;
    }

    public void setPoints(DOMKeypoints points) {
        this.points = points;
    }

    public List<DOMKeypoint> getDOMKeypoints() {
        return this.getPoints().getPoints();
    }
}

