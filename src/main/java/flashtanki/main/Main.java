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
import flashtanki.discord.JdaBot;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import org.apache.log4j.PropertyConfigurator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        try {
            initializeSystem();
            initializeServices();
            startResourceServer();
            startDiscordBot();
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private static void initializeSystem() {
        PropertyConfigurator.configure(Main.class.getClassLoader().getResource("log4j.properties"));
        ConfigurationsLoader.load("");
        initializeFactories();
        UserGroupsLoader.load("groups/");
        setupDatabaseSession();
    }

    private static void initializeFactories() {
        loadGarageItems();
        WeaponsFactory.getInstance().init("weapons/");
        HullsFactory.getInstance().init("hulls/");
        RankUtils.init();
        MapsLoaderService.initFactoryMaps();
    }

    private static void loadGarageItems() {
        GarageItemsLoader.getInstance().loadFromConfig(
                "turrets.json",
                "hulls.json",
                "colormaps.json",
                "inventory.json",
                "modules.json"
        );
    }

    private static void setupDatabaseSession() {
        try (Session session = HibernateService.getSessionFactory().openSession()) {
            session.beginTransaction();
            executeNativeQuery(session);
            session.getTransaction().commit();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private static void executeNativeQuery(Session session) {
        NativeQuery<?> query = session.createNativeQuery("SET NAMES 'utf8' COLLATE 'utf8_general_ci';");
        query.executeUpdate();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
        try {
            ResourceServer.start();
            logSuccess("Resource server started successfully.");
        } catch (IOException e) {
            handleException(e);
        }
    }

    private static void startDiscordBot() {
        try {
            JdaBot.initialize();
            logSuccess("Discord bot started successfully.");
        } catch (InterruptedException | LoginException e) {
            handleException(e);
        }
    }

    private static void handleException(Exception e) {
        Logger logger = Logger.getLogger(Main.class.getName());
        logger.log(Level.SEVERE, "Exception caught", e);
        RemoteDatabaseLogger.error(e);
    }

    private static void logSuccess(String message) {
        Logger logger = Logger.getLogger(Main.class.getName());
        logger.log(Level.INFO, message);
    }
}
