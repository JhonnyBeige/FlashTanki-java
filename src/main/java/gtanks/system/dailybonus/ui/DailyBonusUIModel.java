/*
 * Decompiled with CFR 0.150.
 */
package gtanks.system.dailybonus.ui;

import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.system.dailybonus.BonusListItem;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DailyBonusUIModel {
    public void showBonuses(LobbyManager lobby, List<BonusListItem> bonusesData) {
        JSONObject json = new JSONObject();
        JSONArray items = new JSONArray();
        for (BonusListItem item : bonusesData) {
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

