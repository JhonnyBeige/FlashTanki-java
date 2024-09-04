package flashtanki.battles.tanks.skin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemSkinsObject {
    private String skin;
    private String item;
    private boolean bought;
    private int price;
    private boolean equipped;
    private String title;
    private String desc;
}
