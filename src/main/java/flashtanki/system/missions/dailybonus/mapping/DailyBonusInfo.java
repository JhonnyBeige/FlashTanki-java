package flashtanki.system.missions.dailybonus.mapping;

import flashtanki.system.missions.dailybonus.BonusListItem;
import java.util.List;

public class DailyBonusInfo {
    private int bonusType;
    private List<BonusListItem> bonusList;

    public DailyBonusInfo(int bonusType, List<BonusListItem> bonusList) {
        this.bonusType = bonusType;
        this.bonusList = bonusList;
    }

    public int getBonusType() {
        return bonusType;
    }

    public void setBonusType(int bonusType) {
        this.bonusType = bonusType;
    }

    public List<BonusListItem> getBonusList() {
        return bonusList;
    }

    public void setBonusList(List<BonusListItem> bonusList) {
        this.bonusList = bonusList;
    }

    @Override
    public String toString() {
        return "DailyBonusInfo{" +
                "bonusType=" + bonusType +
                ", bonusList=" + bonusList +
                '}';
    }
}
