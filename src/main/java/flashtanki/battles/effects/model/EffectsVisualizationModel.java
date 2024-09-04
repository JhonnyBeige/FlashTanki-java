/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.battles.effects.model;

import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.effects.Effect;
import flashtanki.battles.spectator.SpectatorController;
import flashtanki.commands.Type;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class EffectsVisualizationModel {
    private final BattlefieldModel bfModel;

    public EffectsVisualizationModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendInitData(BattlefieldPlayerController player) {
        JSONObject _obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (BattlefieldPlayerController _player : this.bfModel.players.values()) {
            if (player == _player) continue;
            ArrayList<Effect> arrayList = _player.tank.activeEffects;
            synchronized (arrayList) {
                for (Effect effect : _player.tank.activeEffects) {
                    JSONObject obj = new JSONObject();
                    obj.put("userID", _player.getUser().getNickname());
                    obj.put("itemIndex", effect.getID());
                    obj.put("durationTime", 60000);
                    array.add(obj);
                }
            }
        }
        _obj.put("effects", array);
        player.send(Type.BATTLE, "init_effects", _obj.toJSONString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendInitData(SpectatorController player) {
        JSONObject _obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (BattlefieldPlayerController _player : this.bfModel.players.values()) {
            ArrayList<Effect> arrayList = _player.tank.activeEffects;
            synchronized (arrayList) {
                for (Effect effect : _player.tank.activeEffects) {
                    JSONObject obj = new JSONObject();
                    obj.put("userID", _player.getUser().getNickname());
                    obj.put("itemIndex", effect.getID());
                    obj.put("durationTime", 60000);
                    array.add(obj);
                }
            }
        }
        _obj.put("effects", array);
        player.sendCommand(Type.BATTLE, "init_effects", _obj.toJSONString());
    }
}

