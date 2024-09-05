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
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.json.simple.parser.ParseException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            initializeSystem();
            initializeServices();
        } catch (Exception ex) {
            ex.printStackTrace();
            RemoteDatabaseLogger.error(ex);
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
            SQLQuery query = session.createSQLQuery("SET NAMES 'utf8' COLLATE 'utf8_general_ci';");
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
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
}
