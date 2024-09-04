package flashtanki.users.garage.containers.list;

import flashtanki.users.garage.items.Item;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "container_assortment")
@Data
public class ContainerItemInfo {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "client_id")
    private String clientId;
    @Column(name = "title_ru")
    private String titleRu;
    @Column(name = "title_en")
    private String titleEn;
    @Column(name = "desc_ru")
    private String descRu;
    @Column(name = "desc_en")
    private String descEn;

    public List<Item> getItems() {
        int items = 0;
        return new ArrayList<>(items);
    }
}
