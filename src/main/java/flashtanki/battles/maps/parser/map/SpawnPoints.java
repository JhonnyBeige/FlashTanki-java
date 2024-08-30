/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlRootElement
 */
package flashtanki.battles.maps.parser.map;

import flashtanki.battles.maps.parser.map.spawn.SpawnPosition;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="spawn-points")
class SpawnPoints {
    private List<SpawnPosition> spawnPositions;

    SpawnPoints() {
    }

    public List<SpawnPosition> getSpawnPositions() {
        return this.spawnPositions;
    }

    @XmlElement(name="spawn-point")
    public void setSpawnPositions(List<SpawnPosition> spawnPositions) {
        this.spawnPositions = spawnPositions;
    }
}

