package flashtanki.users.garage.containers.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_container")
public class UserContainer {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "container_id")
    private Long containerId;
    @Column(name = "count")
    private Integer count;
}
