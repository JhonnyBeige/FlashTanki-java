package flashtanki.system.missions.dailybonus.ui;

import flashtanki.commands.Type;
import flashtanki.lobby.LobbyManager;
import flashtanki.system.missions.dailybonus.BonusListItem;
import flashtanki.system.missions.dailybonus.mapping.DailyBonusInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DailyBonusUIModel {
    public void showBonuses(LobbyManager lobby, DailyBonusInfo bonusInfo) {
        JSONObject json = new JSONObject();
        JSONArray items = new JSONArray();
        for (BonusListItem item : bonusInfo.getBonusList()) {
            JSONObject _item = new JSONObject();
            _item.put("id", item.getBonus().id);
            _item.put("count", item.getCount());
            items.add(_item);
        }
        json.put("items", items);
        lobby.send(Type.LOBBY, "show_bonuses", json.toJSONString());
    }

    public void showCrystalls(LobbyManager lobby, int count) {
        lobby.send(Type.LOBBY, "show_crystalls", String.valueOf(count));
    }

    public void showNoSupplies() {
    }
}
