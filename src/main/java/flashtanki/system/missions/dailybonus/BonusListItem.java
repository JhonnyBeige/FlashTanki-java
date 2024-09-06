package flashtanki.system.missions.dailybonus;

import flashtanki.users.garage.items.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BonusListItem {
    private Item bonus;
    private int count;

    public void addCount(int count) {
        this.count += count;
    }
}
