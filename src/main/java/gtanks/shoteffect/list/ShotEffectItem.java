package gtanks.shoteffect.list;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "shot_effects_assortment")
@Data
public class ShotEffectItem {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "client_id")
    private String clientId;
    @Column(name = "item_id")
    private String itemId;
    @Column(name = "price")
    private int price;
    @Column(name = "title_ru")
    private String titleRu;
    @Column(name = "title_en")
    private String titleEn;
    @Column(name = "desc_ru")
    private String descRu;
    @Column(name = "desc_en")
    private String descEn;
}
