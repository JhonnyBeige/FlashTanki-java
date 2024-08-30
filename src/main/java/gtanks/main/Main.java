package gtanks.main;

import gtanks.RankUtils;
import gtanks.battles.maps.MapsLoaderService;
import gtanks.battles.tanks.loaders.HullsFactory;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.challenges.GetChallengeInfoService;
import gtanks.challenges.UpdateStarsService;
import gtanks.containers.OpenContainerService;
import gtanks.containers.PopulateContainerWindowService;
import gtanks.groups.UserGroupsLoader;
import gtanks.kafka.KafkaTemplateService;
import gtanks.lobby.LobbyManager;
import gtanks.lobby.shop.GiveItemService;
import gtanks.logger.RemoteDatabaseLogger;
import gtanks.main.netty.NettyService;
import gtanks.services.AutoEntryServices;
import gtanks.services.hibernate.HibernateService;
import gtanks.system.SystemBattlesHandler;
import gtanks.system.dailybonus.DailyBonusService;
import gtanks.system.quartz.impl.QuartzServiceImpl;
import gtanks.test.server.configuration.ConfigurationsLoader;
import gtanks.users.garage.GarageItemsLoader;
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
