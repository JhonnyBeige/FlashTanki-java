package flashtanki.system.challenges;

import flashtanki.commands.Type;
import flashtanki.main.kafka.KafkaTemplateService;
import flashtanki.main.kafka.MessageConsumer;
import flashtanki.lobby.LobbyManager;
import flashtanki.services.LobbysServices;
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
