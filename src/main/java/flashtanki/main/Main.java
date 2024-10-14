package flashtanki.main;

import flashtanki.utils.RankUtils;
import flashtanki.battles.maps.MapsLoaderService;
import flashtanki.battles.tanks.loaders.HullsFactory;
import flashtanki.battles.tanks.loaders.WeaponsFactory;
import flashtanki.system.missions.challenges.GetChallengeInfoService;
import flashtanki.system.missions.challenges.UpdateStarsService;
import flashtanki.users.garage.containers.OpenContainerService;
import flashtanki.users.garage.containers.PopulateContainerWindowService;
import flashtanki.users.groups.UserGroupsLoader;
import flashtanki.main.kafka.KafkaTemplateService;
import flashtanki.lobby.shop.GiveItemService;
import flashtanki.logger.remote.RemoteDatabaseLogger;
import flashtanki.main.netty.NettyService;
import flashtanki.services.AutoEntryServices;
import flashtanki.services.hibernate.HibernateService;
import flashtanki.system.SystemBattlesHandler;
import flashtanki.system.missions.dailybonus.DailyBonusService;
import flashtanki.system.quartz.impl.QuartzServiceImpl;
import flashtanki.configurator.server.configuration.ConfigurationsLoader;
import flashtanki.users.garage.GarageItemsLoader;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import flashtanki.discord.JdaBot;
import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] args) {
        try {
            initializeSystem();
            initializeServices();
            startResourceServer();
            startDiscordBot("MTI0OTI4Mjc2ODc4NjAzMDY1Mw.GNhwjn.tGmHM8L0VZjvtZamOrSmWZ690hA0g2ZkTQqkaA");
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private static void initializeSystem() throws IOException, ParseException {
        ConfigurationsLoader.load("");
        initializeFactories();
        UserGroupsLoader.load("groups/");
        setupDatabaseSession();
    }

    private static void initializeFactories() throws IOException, ParseException {
        GarageItemsLoader.getInstance().loadFromConfig(
                "turrets.json",
                "hulls.json",
                "colormaps.json",
                "inventory.json",
                "modules.json"
        );
        WeaponsFactory.getInstance().init("weapons/");
        HullsFactory.getInstance().init("hulls/");
        RankUtils.init();
        MapsLoaderService.initFactoryMaps();
    }

    private static void setupDatabaseSession() {
        try (Session session = HibernateService.getSessionFactory().openSession()) {
            session.beginTransaction();
            NativeQuery<?> query = session.createNativeQuery("SET NAMES 'utf8' COLLATE 'utf8_general_ci';");
            query.executeUpdate();
            session.getTransaction().commit();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private static void initializeServices() {
        QuartzServiceImpl.getInstance();
        DailyBonusService.getInstance();
        AutoEntryServices.getInstance();
        NettyService.getInstance().init();
        GiveItemService.getInstance();
        GetChallengeInfoService.getInstance();
        PopulateContainerWindowService.getInstance();
        OpenContainerService.getInstance();
        KafkaTemplateService.getInstance();
        UpdateStarsService.getInstance();
        SystemBattlesHandler.systemBattlesInit();
    }

    private static void startResourceServer() {
        new Thread(() -> {
            try {
                ResourceServer.start();
            } catch (IOException e) {
                handleException(e);
            }
        }).start();
    }

    private static void startDiscordBot(String token) {
        try {
            JdaBot.initialize(token);
        } catch (InterruptedException | LoginException e) {
            handleException(e);
        }
    }

    private static void handleException(Exception e) {
        e.printStackTrace();
        RemoteDatabaseLogger.error(e);
    }
}
