package gtanks.challenges;

import gtanks.commands.Type;
import gtanks.kafka.KafkaTemplateService;
import gtanks.kafka.MessageConsumer;
import gtanks.lobby.LobbyManager;
import gtanks.services.LobbysServices;
import gtanks.system.localization.Localization;
import gtanks.users.garage.GarageItemsLoader;
import org.apache.kafka.common.protocol.types.Field;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class UpdateStarsService implements MessageConsumer.ExternalMessageListener {
    private KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private static UpdateStarsService instance;

    public static UpdateStarsService getInstance() {
        if (instance == null) {
            instance = new UpdateStarsService();
        }
        return instance;
    }

    private UpdateStarsService() {
        kafkaTemplateService.getConsumer().addListener("update-stars-request", this);
    }

    @Override
    public void onReceive(String message) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        Long userId = Long.valueOf(jsonObject.get("userId").toString());
        String stars = jsonObject.get("stars").toString();
        LobbyManager lobbyManager = LobbysServices.getInstance()
                .getLobbyByUserId(userId);

        if (lobbyManager != null) {
            lobbyManager.send(Type.LOBBY, "stars_count", stars);
        }
    }
}
