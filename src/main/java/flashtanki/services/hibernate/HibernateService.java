/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.services.hibernate;

import flashtanki.captcha.CaptchaService;
import flashtanki.containers.list.ContainerItemInfo;
import flashtanki.containers.users.UserContainer;
import flashtanki.logger.LogObject;
import flashtanki.main.netty.blackip.BlackIP;
import flashtanki.premium.Premium;
import flashtanki.shoteffect.list.ShotEffectItem;
import flashtanki.shoteffect.user.UserShotEffect;
import flashtanki.skin.list.SkinItem;
import flashtanki.skin.user.UserSkin;
import flashtanki.users.User;
import flashtanki.users.friends.Friends;
import flashtanki.users.garage.Garage;
import flashtanki.users.karma.Karma;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateService {
    private static final SessionFactory sessionFactory;

    static {
        try {
            Configuration cfg = new Configuration();

            cfg.addAnnotatedClass(BlackIP.class);
            cfg.addAnnotatedClass(User.class);
            cfg.addAnnotatedClass(LogObject.class);
            cfg.addAnnotatedClass(Karma.class);
            cfg.addAnnotatedClass(Garage.class);
            cfg.addAnnotatedClass(Friends.class);
            cfg.addAnnotatedClass(UserSkin.class);
            cfg.addAnnotatedClass(SkinItem.class);
            cfg.addAnnotatedClass(ContainerItemInfo.class);
            cfg.addAnnotatedClass(UserContainer.class);
            cfg.addAnnotatedClass(Premium.class);
            cfg.addAnnotatedClass(CaptchaService.Captcha.class);
            cfg.addAnnotatedClass(UserShotEffect.class);
            cfg.addAnnotatedClass(ShotEffectItem.class);

            cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            cfg.setProperty("hibernate.connection.url", "jdbc:mysql://127.0.0.1:"
                    + "3306" + "/" + "gtanks");
            cfg.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            cfg.setProperty("hibernate.connection.username", "root");
            cfg.setProperty("hibernate.connection.password", "");
            cfg.setProperty("show_sql", "true");
            cfg.setProperty("hbm2ddl.auto", "update");
            cfg.setProperty("hibernate.current_session_context_class", "thread");
            cfg.setProperty("hibernate.c3p0.acquire_increment", "1");
            cfg.setProperty("hibernate.c3p0.idle_test_period", "100");
            cfg.setProperty("hibernate.c3p0.max_size", "100");
            cfg.setProperty("hibernate.c3p0.max_statements", "0");
            cfg.setProperty("hibernate.c3p0.min_size", "10");
            cfg.setProperty("hibernate.c3p0.testConnectionOnCheckin", "true");
            cfg.setProperty("hibernate.c3p0.timeout", "1800");
            cfg.setProperty("hbm2ddl.auto", "create");
            
            cfg.setProperty("hibernate.jdbc.batch_size", "100");
            cfg.setProperty("hibernate.format_sql", "true");
            cfg.setProperty("hibernate.use_sql_comments", "1800");

            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}