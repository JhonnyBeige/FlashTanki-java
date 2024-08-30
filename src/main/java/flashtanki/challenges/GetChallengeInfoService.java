package flashtanki.challenges;

import flashtanki.commands.Type;
import flashtanki.containers.ContainerSystem;
import flashtanki.kafka.KafkaTemplateService;
import flashtanki.kafka.MessageConsumer;
import flashtanki.lobby.LobbyManager;
import flashtanki.services.LobbysServices;
import flashtanki.shoteffect.ShotEffectSystem;
import flashtanki.skin.SkinSystem;
import flashtanki.system.localization.Localization;
import flashtanki.users.garage.GarageItemsLoader;
import flashtanki.users.garage.items.Item;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GetChallengeInfoService implements MessageConsumer.ExternalMessageListener {
    private KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private SkinSystem skinSystem = SkinSystem.getInstance();
    private ShotEffectSystem shotEffectSystem = ShotEffectSystem.getInstance();
    private ContainerSystem containerSystem = ContainerSystem.getInstance();
    private static GetChallengeInfoService instance;

    public static GetChallengeInfoService getInstance() {
        if (instance == null) {
            instance = new GetChallengeInfoService();
        }
        return instance;
    }

    private GetChallengeInfoService() {
        kafkaTemplateService.getConsumer().addListener("get-challenge-info-response", this);
    }

    @Override
    public void onReceive(String message) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        Long userId = Long.valueOf(jsonObject.get("userId").toString());
        LobbyManager lobbyManager = LobbysServices.getInstance().getLobbyByUserId(userId);
        Localization localization = lobbyManager.getLocalUser().getLocalization();

        JSONArray tiers = (JSONArray) jsonObject.get("tiers");
        for (int i = 0; i < tiers.size(); i++) {
            JSONObject tier = (JSONObject) tiers.get(i);
            JSONObject base = (JSONObject) tier.get("base");
            JSONObject battlePass = (JSONObject) tier.get("battlePass");
            String baseItem = base.get("itemId").toString();
            String battlePassItem = battlePass.get("itemId").toString();

            fillName(baseItem, localization, base);
            fillName(battlePassItem, localization, battlePass);

        }
        lobbyManager.send(Type.LOBBY, "init_challenges_panel", jsonObject.toJSONString());
    }

    private void fillName(String itemFullName, Localization localization, JSONObject base) {
        int endIndex = itemFullName.lastIndexOf("_");
        String itemType = endIndex == -1 ? itemFullName :
                itemFullName.substring(0, endIndex);

        String baseItemTitle;
        if (itemType.equals("crystalls")) {
            baseItemTitle = "Crystals";
        } else if (skinSystem.existSkin(itemFullName)) {
            baseItemTitle = "Skin";
        } else if (shotEffectSystem.existShotEffect(itemFullName)) {
            baseItemTitle = "ShotEffect";
        } else if (containerSystem.existContainer(itemFullName)) {
            baseItemTitle = "Container";
        } else if (GarageItemsLoader.getInstance().items.containsKey(itemType)) {
            Item item1 = GarageItemsLoader.getInstance().items
                    .get(itemType);
            baseItemTitle = item1
                    .name
                    .localizatedString(localization);
        } else {
            baseItemTitle = "Unknown";
        }

        base.put("itemName", baseItemTitle);
    }
}
