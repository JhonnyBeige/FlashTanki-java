package gtanks.captcha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "captcha")
public class Captcha {
    @Id
    @GeneratedValue(generator = "increment")
    @Column(name = "id")
    private Long id;
    @Column(name = "code")
    private String code;
}
