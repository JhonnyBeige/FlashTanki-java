/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.users;

import flashtanki.utils.StringUtils;
import flashtanki.groups.UserGroup;
import flashtanki.logger.RemoteDatabaseLogger;
import flashtanki.users.premium.PremiumService;
import flashtanki.system.localization.Localization;
import flashtanki.users.anticheat.AntiCheatData;
import flashtanki.users.garage.Garage;
import flashtanki.users.karma.Karma;
import flashtanki.users.locations.UserLocation;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@org.hibernate.annotations.Entity
@Table(name = "users")
public class User
        implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid", unique = true, nullable = false)
    private long id;
    @Column(name = "nickname", unique = true, nullable = false)
    private String nickname;
    @Column(name = "password", unique = true, nullable = false)
    private String password;
    @Column(name = "\"rank\"", unique = true, nullable = false)
    private int rang = 0;
    @Column(name = "score", unique = true, nullable = false)
    private int score = 0;
    @Column(name = "crystalls", unique = true, nullable = false)
    private int crystall = 0;
    @Column(name = "next_score", unique = true, nullable = false)
    private int nextScore = 100;
    @Column(name = "place", unique = true, nullable = false)
    private int place = 0;
    @Column(name = "rating", unique = true, nullable = false)
    private int rating = 1;
    @Column(name = "email_confirmed", unique = true, nullable = false)
    @Getter
    @Setter
    private boolean isEmailConfirmed = false;
    @Column(name = "email", unique = true, nullable = true)
    private String email = "default@gtanks.com";
    @Setter
    @Getter
    @Column(name = "email_confirmation_code", unique = false, nullable = true)
    private String emailConfirmationCode;
    @Column(name = "last_ip", unique = false, nullable = false)
    private String lastIP;
    @Column(name = "user_type", unique = true, nullable = false)
    @Enumerated(value = EnumType.ORDINAL)
    private TypeUser type = TypeUser.DEFAULT;
    @Column(name = "last_issue_bonus", nullable = true)
    private Date lastIssueBonus;
    @Transient
    private UserLocation userLocation;
    @Transient
    private Karma karma;
    @Transient
    private Garage garage;
    @Transient
    private int warnings;
    @Transient
    private AntiCheatData antiCheatData = new AntiCheatData();
    @Transient
    private UserGroup userGroup;
    @Transient
    private Localization localization;
    @Transient
    private String lastFriendRequest;

    public User(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
        this.garage = new Garage();
    }

    public User() {
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRang() {
        return this.rang;
    }

    public void setRang(int rang) {
        this.rang = rang;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        if(score <= 0) {
            RemoteDatabaseLogger.error( new Exception("Score value must be greater than 0"));
        }
        // Major and above
        if (this.rang > 18) {
            this.score += score * (PremiumService.getInstance().getPremiumTime(this.id).isActivated() ? 2 : 1);
        }else {
            this.score += score * (PremiumService.getInstance().getPremiumTime(this.id).isActivated() ? 2 : 1);
        }
    }

    public int getCrystall() {
        return this.crystall;
    }

    public void setCrystall(int crystall) {
        this.crystall = crystall;
    }

    public void addCrystall(int crystall) {
        this.crystall += crystall;
    }

    public int getNextScore() {
        return this.nextScore;
    }

    public void setNextScore(int nextScore) {
        this.nextScore = nextScore;
    }

    public int getPlace() {
        return this.place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getRating() {
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Garage getGarage() {
        return this.garage;
    }

    public void setGarage(Garage garage) {
        this.garage = garage;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TypeUser getType() {
        return this.type;
    }

    public void setType(TypeUser type) {
        this.type = type;
    }

    public UserLocation getUserLocation() {
        return this.userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public Karma getKarma() {
        return this.karma;
    }

    public void setKarma(Karma karma) {
        this.karma = karma;
    }

    public int getWarnings() {
        return this.warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public void addWarning() {
        ++this.warnings;
    }

    public AntiCheatData getAntiCheatData() {
        return this.antiCheatData;
    }

    public void setAntiCheatData(AntiCheatData antiCheatData) {
        this.antiCheatData = antiCheatData;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLastIP() {
        return this.lastIP;
    }

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public void setLastFriendRequest(String lastFriendRequested) {
        this.lastFriendRequest = lastFriendRequested;
    }

    public String getLastFriendRequest() {
        return this.lastFriendRequest;
    }

    public UserGroup getUserGroup() {
        return this.userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public Date getLastIssueBonus() {
        return this.lastIssueBonus;
    }

    public void setLastIssueBonus(Date lastIssueBonus) {
        this.lastIssueBonus = lastIssueBonus;
    }

    public String toString() {
        return StringUtils.concatStrings(String.valueOf(this.rang), " ", this.nickname, " ", this.password);
    }

    public int getUserTypeInt(String userTypeString) {
        if ("default".equals(userTypeString)) {
            return 0;
        } else if ("communitymanager".equals(userTypeString)) {
            return 1;
        } else if ("headadministrator".equals(userTypeString)) {
            return 2;
        } else if ("chatadministrator".equals(userTypeString)) {
            return 3;
        } else if ("chatmoderator".equals(userTypeString)) {
            return 4;
        } else if ("chatmoderatorcandidate".equals(userTypeString)) {
            return 5;
        } else if ("battleadministrator".equals(userTypeString)) {
            return 6;
        } else if ("battlemoderator".equals(userTypeString)) {
            return 7;
        } else if ("battlemoderatorcandidate".equals(userTypeString)) {
            return 8;
        } else if ("eventsadministrator".equals(userTypeString)) {
            return 9;
        } else if ("eventsorganizer".equals(userTypeString)) {
            return 10;
        } else if ("spectator".equals(userTypeString)) {
            return 0;
        } else {
            return 0;
        }
    }

    public Localization getLocalization() {
        return this.localization;
    }

    public void setLocalization(Localization localization) {
        this.localization = localization;
    }
}
