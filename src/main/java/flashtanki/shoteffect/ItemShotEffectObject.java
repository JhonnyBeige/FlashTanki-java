package flashtanki.shoteffect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemShotEffectObject {
    private String shotEffect;
    private String item;
    private boolean bought;
    private int price;
    private boolean equipped;
    private String title;
    private String desc;
}
