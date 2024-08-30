package gtanks.lobby.shop;

import gtanks.containers.ContainerSystem;
import gtanks.json.JSONUtils;
import gtanks.kafka.KafkaTemplateService;
import gtanks.kafka.MessageConsumer;
import gtanks.lobby.LobbyManager;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.premium.PremiumService;
import gtanks.services.LobbysServices;
import gtanks.shoteffect.ShotEffectSystem;
import gtanks.skin.SkinSystem;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.garage.GarageItemsLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GiveItemService implements MessageConsumer.ExternalMessageListener {
    private static GiveItemService instance;
    private final KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private LobbysServices lobbysServices = LobbysServices.getInstance();
    private DatabaseManager databaseManager = DatabaseManagerImpl.instance();
    private SkinSystem skinSystem = SkinSystem.getInstance();
    private ShotEffectSystem shotEffectSystem = ShotEffectSystem.getInstance();
    private ContainerSystem containerSystem = ContainerSystem.getInstance();

    public static GiveItemService getInstance() {
        if (instance == null) {
            instance = new GiveItemService();
        }
        return instance;
    }

    private GiveItemService() {
        kafkaTemplateService.getConsumer()
                .addListener("give-item-request", this);
    }

    @SneakyThrows
    @Override
    public void onReceive(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        GiveItemRequest request = objectMapper.readValue(message, GiveItemRequest.class);
        String fullItemId = request.getItemId();
        int endIndex = fullItemId.lastIndexOf("_");
        String itemType = endIndex == -1 ? fullItemId : fullItemId.substring(0, endIndex);

        if (fullItemId.equals("premium")) {
            PremiumService.getInstance().activatePremium(request.getUserId(), (long) request.getCount());
        } else if (fullItemId.equals("crystalls")) {
            Optional.ofNullable(lobbysServices.getLobbyByUserId(request.getUserId()))
                    .ifPresentOrElse(lobbyManager -> lobbyManager.addCrystall((int) request.count),
                            () -> {
                                User user = databaseManager.getUserById(request.getUserId());
                                user.addCrystall(request.count);
                                databaseManager.update(user);
                            });
        } else if (skinSystem.existSkin(fullItemId)) {
            skinSystem.giveSkin(request.getUserId(), fullItemId, () -> {
            }, () -> {
            });
        } else if (shotEffectSystem.existShotEffect(fullItemId)) {
            shotEffectSystem.giveShotEffect(request.getUserId(), fullItemId, () -> {
            }, () -> {
            });
        } else if (containerSystem.existContainer(fullItemId)) {
            containerSystem.giveContainer(request.getUserId(), fullItemId, request.getCount());
        } else if (GarageItemsLoader.getInstance().items.containsKey(itemType)) {
            Garage garageByUser = Optional.ofNullable(lobbysServices.getLobbyByUserId(request.getUserId()))
                    .map(lobbyManager -> lobbyManager.getLocalUser().getGarage())
                    .orElseGet(() -> {
                        User localUser = databaseManager.getUserById(request.getUserId());
                        Garage garage = databaseManager.getGarageByUser(localUser);
                        try {
                            garage.unparseJSONData();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return garage;
                    });

            garageByUser.giveItem(fullItemId, (int) request.getCount(), () -> {
            }, () -> {
            });
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GiveItemRequest {
        private Long userId;
        private String itemId;
        private int count;
    }
}
