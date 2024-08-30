/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlRootElement
 */
package flashtanki.battles.maps.parser.map;

import flashtanki.battles.maps.parser.map.bonus.BonusRegion;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="bonus-regions")
class BonusRegions {
    private List<BonusRegion> bonusRegions;

    BonusRegions() {
    }

    public List<BonusRegion> getBonusRegions() {
        return this.bonusRegions;
    }

    @XmlElement(name="bonus-region")
    public void setBonusRegions(List<BonusRegion> bonusRegions) {
        this.bonusRegions = bonusRegions;
    }
}

