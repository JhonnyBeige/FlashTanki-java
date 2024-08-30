// 
// Decompiled by Procyon v0.5.36
// 

package gtanks.users.friends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.persistence.Column;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@org.hibernate.annotations.Entity
@Table(name = "friends")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Friends
{
    @Id
    @Column(name = "user_id")
    private long userId;
    @Column(name = "incoming")
    private String incoming;
    @Column(name = "outgoing")
    private String outgoing;
    @Column(name = "accepted")
    private String accepted;
}