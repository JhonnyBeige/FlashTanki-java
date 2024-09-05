/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.json;

import flashtanki.utils.StringUtils;
import flashtanki.battles.BattlefieldModel;
import flashtanki.battles.BattlefieldPlayerController;
import flashtanki.battles.TankKillModel;
import flashtanki.battles.bonuses.Bonus;
import flashtanki.battles.chat.BattleChatMessage;
import flashtanki.battles.ctf.CTFModel;
import flashtanki.battles.ctf.flags.FlagServer;
import flashtanki.battles.maps.Map;
import flashtanki.battles.maps.MapsLoaderService;
import flashtanki.battles.mines.ServerMine;
import flashtanki.battles.tanks.Tank;
import flashtanki.battles.tanks.loaders.HullsFactory;
import flashtanki.battles.tanks.math.Vector3;
import flashtanki.battles.tanks.weapons.IEntity;
import flashtanki.battles.tanks.weapons.WeaponWeakeningData;
import flashtanki.battles.tanks.weapons.flamethrower.FlamethrowerEntity;
import flashtanki.battles.tanks.weapons.frezee.FrezeeEntity;
import flashtanki.battles.tanks.weapons.isida.IsidaEntity;
import flashtanki.battles.tanks.weapons.ricochet.RicochetEntity;
import flashtanki.battles.tanks.weapons.shaft.ShaftEntity;
import flashtanki.battles.tanks.weapons.thunder.ThunderEntity;
import flashtanki.battles.tanks.weapons.twins.TwinsEntity;
import flashtanki.collections.FastHashMap;
import flashtanki.lobby.battles.BattleInfo;
import flashtanki.lobby.battles.BattlesList;
import flashtanki.lobby.chat.ChatMessage;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.users.premium.PremiumService;
import flashtanki.services.AutoEntryServices;
import flashtanki.battles.tanks.shoteffect.ShotEffectSystem;
import flashtanki.battles.tanks.shoteffect.list.ShotEffectItem;
import flashtanki.battles.tanks.shoteffect.user.UserShotEffect;
import flashtanki.battles.tanks.skin.SkinSystem;
import flashtanki.battles.tanks.skin.list.SkinItem;
import flashtanki.battles.tanks.skin.user.UserSkin;
import flashtanki.users.TypeUser;
import flashtanki.users.User;
import flashtanki.users.garage.Garage;
import flashtanki.users.garage.GarageItemsLoader;
import flashtanki.users.garage.items.Item;
import flashtanki.users.garage.items.PropertyItem;
import flashtanki.users.garage.items.modification.ModificationInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import lombok.SneakyThrows;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JSONUtils {
    private static AutoEntryServices autoEntryServices = AutoEntryServices.getInstance();
    private static DatabaseManager databaseManager = DatabaseManagerImpl.instance();
    private static BattlesList battlesList = BattlesList.getInstance();
    private static SkinSystem skinSystem = SkinSystem.getInstance();
    private static ShotEffectSystem shotEffectSystem = ShotEffectSystem.getInstance();

    public static String parseConfiguratorEntity(Object entity, Class clazz) {
        JSONObject jobj = new JSONObject();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                jobj.put(field.getName(), field.get(entity));
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return jobj.toJSONString();
    }

    public static String parseInitMinesComand(FastHashMap<BattlefieldPlayerController, ArrayList<ServerMine>> mines) {
        JSONObject jobj = new JSONObject();
        JSONArray array = new JSONArray();
        for (ArrayList<ServerMine> userMines : mines.values()) {
            for (ServerMine mine : userMines) {
                JSONObject _mine = new JSONObject();
                _mine.put("ownerId", mine.getOwner().tank.id);
                _mine.put("mineId", mine.getId());
                _mine.put("x", Float.valueOf(mine.getPosition().x));
                _mine.put("y", Float.valueOf(mine.getPosition().y));
                _mine.put("z", Float.valueOf(mine.getPosition().z));
                array.add(_mine);
            }
        }
        jobj.put("mines", array);
        return jobj.toJSONString();
    }

    public static String parsePutMineComand(ServerMine mine) {
        JSONObject jobj = new JSONObject();
        jobj.put("mineId", mine.getId());
        jobj.put("userId", mine.getOwner().tank.id);
        jobj.put("x", Float.valueOf(mine.getPosition().x));
        jobj.put("y", Float.valueOf(mine.getPosition().y));
        jobj.put("z", Float.valueOf(mine.getPosition().z));
        return jobj.toJSONString();
    }

    public static String parseQuests() {
        return "{\n" +
                "    \"changeCost\": 0,\n" +
                "    \"quest1\": {\n" +
                "        \"description\": \"Gain crystals in battles\",\n" +
                "        \"id\": \"win_cry\",\n" +
                "        \"target_progress\": \"2500\",\n" +
                "        \"progress\": \"0\",\n" +
                "        \"prizes\": [\"Crystalls × 600\", \"Double damage × 10\"]\n" +
                "    },\n" +
                "    \"quest2\": {\n" +
                "        \"description\": \"Capture flags\",\n" +
                "        \"id\": \"captureTheFlag\",\n" +
                "        \"target_progress\": \"20\",\n" +
                "        \"progress\": 0,\n" +
                "        \"prizes\": [\"Crystalls × 800\", \"Repair kit × 5\"]\n" +
                "    },\n" +
                "    \"quest3\": {\n" +
                "        \"description\": \"Earn score in battles\",\n" +
                "        \"id\": \"gainScore\",\n" +
                "        \"target_progress\": \"750\",\n" +
                "        \"progress\": 0,\n" +
                "        \"prizes\": [\"Crystalls × 500\", \"Double armor × 10\"]\n" +
                "    }\n" +
                "}";
    }

    public static String parseInitInventoryComand(Garage garage) {
        JSONObject jobj = new JSONObject();
        JSONArray array = new JSONArray();
        for (Item item : garage.getInventoryItems()) {
            JSONObject io = new JSONObject();
            io.put("id", item.id);
            io.put("count", item.count);
            io.put("slotId", item.index);
            io.put("itemEffectTime", item.id.equals("mine") ? 20 : (item.id.equals("health") ? 20 : 55));
            io.put("itemRestSec", 10);
            array.add(io);
        }
        jobj.put("items", array);
        return jobj.toJSONString();
    }

    public static String parseDisabledInventory(Garage garage) {
        JSONObject jobj = new JSONObject();
        JSONArray array = new JSONArray();
        jobj.put("items", array);
        return jobj.toJSONString();
    }

    public static String parseRemovePlayerComand(BattlefieldPlayerController player) {
        JSONObject jobj = new JSONObject();
        jobj.put("battleId", player.battle.battleInfo.battleId);
        jobj.put("id", player.getUser().getNickname());
        return jobj.toJSONString();
    }

    public static String parseRemovePlayerComand(String userId, String battleid) {
        JSONObject jobj = new JSONObject();
        jobj.put("battleId", battleid);
        jobj.put("id", userId);
        return jobj.toJSONString();
    }

    public static String parseAddPlayerComand(BattlefieldPlayerController player, BattleInfo battleInfo) {
        JSONObject obj = new JSONObject();
        obj.put("battleId", battleInfo.battleId);
        obj.put("id", player.getUser().getNickname());
        obj.put("kills", player.statistic.getScore());
        obj.put("name", player.getUser().getNickname());
        obj.put("rank", player.getUser().getRang() + 1);
        obj.put("type", player.playerTeamType);
        return obj.toJSONString();
    }

    public static String parseDropFlagCommand(FlagServer flag) {
        JSONObject obj = new JSONObject();
        obj.put("x", Float.valueOf(flag.position.x));
        obj.put("y", Float.valueOf(flag.position.y));
        obj.put("z", Float.valueOf(flag.position.z));
        obj.put("flagTeam", flag.flagTeamType);
        return obj.toJSONString();
    }

    public static String parseCTFModelData(BattlefieldModel model) {
        JSONObject obj = new JSONObject();
        CTFModel ctfModel = model.ctfModel;
        JSONObject basePosBlue = new JSONObject();
        basePosBlue.put("x", Float.valueOf(model.battleInfo.map.flagBluePosition.x));
        basePosBlue.put("y", Float.valueOf(model.battleInfo.map.flagBluePosition.y));
        basePosBlue.put("z", Float.valueOf(model.battleInfo.map.flagBluePosition.z));
        JSONObject basePosRed = new JSONObject();
        basePosRed.put("x", Float.valueOf(model.battleInfo.map.flagRedPosition.x));
        basePosRed.put("y", Float.valueOf(model.battleInfo.map.flagRedPosition.y));
        basePosRed.put("z", Float.valueOf(model.battleInfo.map.flagRedPosition.z));
        JSONObject posBlue = new JSONObject();
        posBlue.put("x", Float.valueOf(ctfModel.getBlueFlag().position.x));
        posBlue.put("y", Float.valueOf(ctfModel.getBlueFlag().position.y));
        posBlue.put("z", Float.valueOf(ctfModel.getBlueFlag().position.z));
        JSONObject posRed = new JSONObject();
        posRed.put("x", Float.valueOf(ctfModel.getRedFlag().position.x));
        posRed.put("y", Float.valueOf(ctfModel.getRedFlag().position.y));
        posRed.put("z", Float.valueOf(ctfModel.getRedFlag().position.z));
        obj.put("basePosBlueFlag", basePosBlue);
        obj.put("basePosRedFlag", basePosRed);
        obj.put("posBlueFlag", posBlue);
        obj.put("posRedFlag", posRed);
        obj.put("blueFlagCarrierId",
                ctfModel.getBlueFlag().owner == null ? null : ctfModel.getBlueFlag().owner.tank.id);
        obj.put("redFlagCarrierId", ctfModel.getRedFlag().owner == null ? null : ctfModel.getRedFlag().owner.tank.id);
        return obj.toJSONString();
    }

    public static String parseUpdateCoundPeoplesCommand(BattleInfo battle) {
        JSONObject obj = new JSONObject();
        obj.put("battleId", battle.battleId);
        obj.put("redPeople", battle.redPeople);
        obj.put("bluePeople", battle.bluePeople);
        return obj.toJSONString();
    }

    public static String parseChallenges() {
        return "{\n" +
        "  \"stars\": 10,\n" +
        "  \"isBattlePassActive\": \"true\",\n" +
        "  \"leftMinutes\": 123,\n" +
        "  \"tiers\": [\n" +
        "    {\n" +
        "      \"stars\": 10,\n" +
        "      \"base\": {\n" +
        "        \"itemId\": \"armor_m0\",\n" +
        "        \"itemName\": \"Double Armor\",\n" +
        "        \"count\": \"10\"\n" +
        "      },\n" +
        "      \"battlePass\": {\n" +
        "        \"itemId\": \"armor_m0\",\n" +
        "        \"itemName\": \"Double Armor\",\n" +
        "        \"count\": 20\n" +
        "      }\n" +
        "    },\n" +
        "    {\n" +
        "      \"stars\": 15,\n" +
        "      \"base\": {\n" +
        "        \"itemId\": \"armor_m0\",\n" +
        "        \"itemName\": \"Double Armor\",\n" +
        "        \"count\": \"50\"\n" +
        "      },\n" +
        "      \"battlePass\": {\n" +
        "        \"itemId\": \"armor_m0\",\n" +
        "        \"itemName\": \"Double Armor\",\n" +
        "        \"count\": 100\n" +
        "      }\n" +
        "    }\n" +
        "  ]\n" +
        "}";
    }

    public static String parseFishishBattle(FastHashMap<String, BattlefieldPlayerController> players,
            int timeToRestart) {
        JSONObject obj = new JSONObject();
        JSONArray users = new JSONArray();
        obj.put("time_to_restart", timeToRestart);
        if (players == null) {
            return obj.toString();
        }
        for (BattlefieldPlayerController bpc : players.values()) {
            JSONObject stat = new JSONObject();
            stat.put("kills", bpc.statistic.getKills());
            stat.put("deaths", bpc.statistic.getDeaths());
            stat.put("id", bpc.getUser().getNickname());
            stat.put("rank", bpc.getUser().getRang() + 1);
            stat.put("prize", bpc.statistic.getPrize());
            stat.put("team_type", bpc.playerTeamType);
            stat.put("score", bpc.statistic.getScore());
            stat.put("weapon", bpc.getGarage().mountTurret.id);
            stat.put("isPremium", PremiumService.getInstance().getPremiumTime(bpc.getUser().getId()).isActivated());
            users.add(stat);
        }
        obj.put("users", users);
        return obj.toString();
    }

    public static String parsePlayerStatistic(BattlefieldPlayerController player) {
        JSONObject obj = new JSONObject();
        obj.put("kills", player.statistic.getKills());
        obj.put("deaths", player.statistic.getDeaths());
        obj.put("id", player.getUser().getNickname());
        obj.put("rank", player.getUser().getRang() + 1);
        obj.put("team_type", player.playerTeamType);
        obj.put("score", player.statistic.getScore());
        obj.put("weapon", player.getGarage().mountTurret.id);
        //FIXME dont call database
        obj.put("isPremium", PremiumService.getInstance().getPremiumTime(player.getUser().getId()).isActivated());
        return obj.toString();
    }

    public static String parseSpawnCommand(BattlefieldPlayerController bpc, Vector3 pos) {
        JSONObject obj = new JSONObject();
        if (bpc == null || bpc.tank == null) {
            return null;
        }
        obj.put("tank_id", bpc.tank.id);
        obj.put("health", bpc.tank.healthPoints);
        obj.put("speed", Float.valueOf(bpc.tank.speed));
        obj.put("turn_speed", Float.valueOf(bpc.tank.turnSpeed));
        obj.put("turret_rotation_speed", Float.valueOf(bpc.tank.turretRotationSpeed));
        obj.put("team_type", bpc.playerTeamType);
        obj.put("x", Float.valueOf(pos.x));
        obj.put("y", Float.valueOf(pos.y));
        obj.put("z", Float.valueOf(pos.z));
        obj.put("rot", pos.rot);
        return obj.toString();
    }

    public static String parseBattleData(BattlefieldModel model) {
        JSONObject obj = new JSONObject();
        JSONArray users = new JSONArray();
        obj.put("name", model.battleInfo.name);
        obj.put("fund", model.tanksKillModel.getBattleFund());
        obj.put("scoreLimit", model.battleInfo.battleType.equals("CTF") ? model.battleInfo.numFlags
                : (model.battleInfo.battleType.equals("DOM") ? model.battleInfo.numFlags : model.battleInfo.numKills));
        obj.put("timeLimit", model.battleInfo.time);
        obj.put("currTime", model.getTimeLeft());
        obj.put("score_red", model.battleInfo.scoreRed);
        obj.put("score_blue", model.battleInfo.scoreBlue);
        obj.put("team", model.battleInfo.team);
        for (BattlefieldPlayerController bpc : model.players.values()) {
            JSONObject usr = new JSONObject();
            usr.put("nickname", bpc.parentLobby.getLocalUser().getNickname());
            usr.put("rank", bpc.parentLobby.getLocalUser().getRang() + 1);
            usr.put("teamType", bpc.playerTeamType);
            usr.put("weapon", bpc.getGarage().mountTurret.id);
            usr.put("isPremium", PremiumService.getInstance().getPremiumTime(bpc.getUser().getId()).isActivated());
            users.add(usr);
        }
        obj.put("users", users);
        return obj.toJSONString();
    }

    public static String parseUserToJSON(User user) {
        JSONObject obj = new JSONObject();
        obj.put("userId", user.getId());
        obj.put("name", user.getNickname());
        obj.put("crystall", user.getCrystall());
        obj.put("email", user.getEmail());
        obj.put("tester", user.getType() != TypeUser.DEFAULT);
        obj.put("next_score", user.getNextScore());
        obj.put("place", user.getPlace());
        obj.put("rang", user.getRang() + 1);
        obj.put("rating", user.getRating());
        obj.put("score", user.getScore());
        obj.put("isEmailConfirmed", user.isEmailConfirmed());
        obj.put("friendsCachedId", user.getLastFriendRequest());
        obj.put("isPremium", PremiumService.getInstance().getPremiumTime(user.getId()).isActivated());
        return obj.toJSONString();
    }

    public static JSONObject parseUserToJSONObject(User user) {
        JSONObject obj = new JSONObject();
        obj.put("name", user.getNickname());
        obj.put("crystall", user.getCrystall());
        obj.put("email", user.getEmail());
        obj.put("tester", user.getType() != TypeUser.DEFAULT);
        obj.put("next_score", user.getNextScore());
        obj.put("place", user.getPlace());
        obj.put("rang", user.getRang() + 1);
        obj.put("rating", user.getRating());
        obj.put("score", user.getScore());
        obj.put("friendsCachedId", user.getLastFriendRequest());
        return obj;
    }

    public static String parseChatLobbyMessage(ChatMessage msg) {
        JSONObject obj = new JSONObject();
        obj.put("userId", msg.user.getId());
        obj.put("name", msg.user.getNickname());
        obj.put("isPremium", msg.isPremium);
        obj.put("rang", msg.user.getRang() + 1);
        obj.put("message", msg.message);
        obj.put("addressed", msg.addressed);
        obj.put("nameTo", msg.userTo == null ? "NULL" : msg.userTo.getNickname());
        obj.put("isPremiumTo", msg.isPremiumTo);
        obj.put("rangTo", msg.userTo == null ? 0 : msg.userTo.getRang() + 1);
        obj.put("system", msg.system);
        obj.put("yellow", msg.yellowMessage);
        obj.put("chatPermissions", msg.user.getUserTypeInt(msg.user.getType().toString()));
        return obj.toJSONString();
    }

    private static JSONObject parseChatLobbyMessageObject(ChatMessage msg) {
        JSONObject obj = new JSONObject();
        obj.put("name", msg.user == null ? "" : msg.user.getNickname());
        obj.put("isPremium", msg.isPremium);
        obj.put("rang", msg.user == null ? 0 : msg.user.getRang() + 1);
        obj.put("message", msg.message);
        obj.put("addressed", msg.addressed);
        obj.put("nameTo", msg.userTo == null ? "" : msg.userTo.getNickname());
        obj.put("rangTo", msg.userTo == null ? 0 : msg.userTo.getRang() + 1);
        obj.put("isPremiumTo", msg.isPremiumTo);
        obj.put("system", msg.system);
        if (msg.user != null) {
            obj.put("chatPermissions", msg.user.getUserTypeInt(msg.user.getType().toString()));
        }
        obj.put("yellow", msg.yellowMessage);
        return obj;
    }

    public static String parseChatLobbyMessages(Collection<ChatMessage> messages) {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (ChatMessage msg : messages) {
            array.add(JSONUtils.parseChatLobbyMessageObject(msg));
        }
        obj.put("messages", array);
        String communicator = obj.toJSONString() + ";" + parseNews();
        return communicator;
    }

    public static String parseNews() {
        return NewsFactory.getData();
    }

    public static String parseGarageUser(User user) {
        try {
            Garage garage = user.getGarage();
            java.util.Map<Long, UserSkin> mountedUserSkins = skinSystem.getMountedUserSkins(user.getId());
            java.util.Map<Long, SkinItem> allSkins = skinSystem.getAllSkins();
            java.util.Map<Long, UserShotEffect> mountedUserShotEffects = shotEffectSystem
                    .getMountedUserShotEffects(user.getId());
            java.util.Map<Long, ShotEffectItem> allShotEffects = shotEffectSystem.getAllShotEffects();
            JSONObject obj = new JSONObject();
            JSONArray array = new JSONArray();
            for (Item item : garage.items) {
                int n;
                int n2;
                Object[] arrobject;
                JSONObject i = new JSONObject();
                JSONArray properts = new JSONArray();
                JSONArray modification = new JSONArray();
                i.put("id", item.id);
                i.put("name", item.name.localizatedString(user.getLocalization()));
                i.put("description", item.description.localizatedString(user.getLocalization()));
                i.put("isInventory", JSONUtils.boolToString(item.isInventory));
                i.put("index", item.index);
                int value = Integer.parseInt(item.itemType.toString());
                i.put("type", value);
                i.put("modificationID", item.modificationIndex);
                i.put("next_price", item.nextPrice);
                i.put("next_rank", item.nextRankId);
                i.put("price", item.price);
                i.put("rank", item.rankId);
                i.put("count", item.count);
                i.put("microUpgrades", item.microUpgrades);
                i.put("microUpgradePrice", item.microUpgradePrice);

                i.put("has_skins", allSkins.values().stream()
                        .anyMatch(skinItem -> Objects.equals(skinItem.getItemId(), item.id)));
                i.put("equippedSkin", mountedUserSkins.values().stream()
                        .map(UserSkin::getSkinId)
                        .map(allSkins::get)
                        .filter(skin -> Objects.equals(skin.getItemId(), item.id))
                        .findFirst()
                        .map(SkinItem::getClientId)
                        .orElse(null));
                i.put("has_shot_effects", allShotEffects.values().stream()
                        .anyMatch(shotEffectItem -> Objects.equals(shotEffectItem.getItemId(), item.id)));
                i.put("equippedShotEffect", mountedUserShotEffects.values().stream()
                        .map(UserShotEffect::getShotEffectId)
                        .map(allShotEffects::get)
                        .filter(shotEffect -> Objects.equals(shotEffect.getItemId(), item.id))
                        .findFirst()
                        .map(ShotEffectItem::getClientId)
                        .orElse(null));

                if (item.propetys != null) {
                    arrobject = item.propetys;
                    n2 = item.propetys.length;
                    for (n = 0; n < n2; ++n) {
                        Object prop = arrobject[n];
                        if (prop == null || ((PropertyItem) prop).property == null)
                            continue;
                        properts.add(JSONUtils.parseProperty((PropertyItem) prop));
                    }
                }
                if (item.modifications != null) {
                    arrobject = item.modifications;
                    n2 = item.modifications.length;
                    for (n = 0; n < n2; ++n) {
                        Object mod = arrobject[n];
                        JSONObject m = new JSONObject();
                        JSONArray prop = new JSONArray();
                        m.put("previewId", ((ModificationInfo) mod).previewId);
                        m.put("price", ((ModificationInfo) mod).price);
                        m.put("rank", ((ModificationInfo) mod).rank);
                        if (((ModificationInfo) mod).propertys != null) {
                            PropertyItem[] arrpropertyItem = ((ModificationInfo) mod).propertys;
                            int n3 = ((ModificationInfo) mod).propertys.length;
                            for (int j = 0; j < n3; ++j) {
                                PropertyItem a = arrpropertyItem[j];
                                if (a == null || a.property == null)
                                    continue;
                                prop.add(JSONUtils.parseProperty(a));
                            }
                        }
                        m.put("properts", prop);
                        modification.add(m);
                    }
                }
                i.put("properts", properts);
                i.put("modification", modification);
                array.add(i);
            }
            obj.put("items", array);
            String response = obj.toString();
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String parseShowProfile(User user) {
        JSONObject obj = new JSONObject();
        obj.put("isComfirmEmail", false);
        obj.put("emailNotice", false);
        return obj.toJSONString();
    }

    public static String parseMarketItems(User user) {
        Garage garage = user.getGarage();
        JSONObject json = new JSONObject();
        JSONArray jarray = new JSONArray();
        for (Item item : GarageItemsLoader.getInstance().items.values()) {
            if (!garage.containsItem(item.id) && !item.specialItem) {
                int n;
                int n2;
                Object[] arrobject;
                JSONObject i = new JSONObject();
                JSONArray properts = new JSONArray();
                JSONArray modification = new JSONArray();
                i.put("id", item.id);
                i.put("name", item.name.localizatedString(user.getLocalization()));
                i.put("description", item.description.localizatedString(user.getLocalization()));
                i.put("isInventory", item.isInventory);
                i.put("index", item.index);
                int value = Integer.parseInt(item.itemType.toString());
                i.put("type", value);
                i.put("modificationID", 0);
                i.put("next_price", item.nextPrice);
                i.put("next_rank", item.nextRankId);
                i.put("price", item.price);
                i.put("rank", item.rankId);
                i.put("microUpgradePrice", item.microUpgradePrice);
                i.put("microUpgrades", item.microUpgrades);
                if (item.propetys != null) {
                    arrobject = item.propetys;
                    n2 = item.propetys.length;
                    for (n = 0; n < n2; ++n) {
                        Object prop = arrobject[n];
                        properts.add(JSONUtils.parseProperty((PropertyItem) prop));
                    }
                }
                if (item.modifications != null) {
                    arrobject = item.modifications;
                    n2 = item.modifications.length;
                    for (n = 0; n < n2; ++n) {
                        Object mod = arrobject[n];
                        JSONObject m = new JSONObject();
                        JSONArray prop = new JSONArray();
                        m.put("previewId", ((ModificationInfo) mod).previewId);
                        m.put("price", ((ModificationInfo) mod).price);
                        m.put("rank", ((ModificationInfo) mod).rank);
                        if (((ModificationInfo) mod).propertys != null) {
                            PropertyItem[] arrpropertyItem = ((ModificationInfo) mod).propertys;
                            int n3 = ((ModificationInfo) mod).propertys.length;
                            for (int j = 0; j < n3; ++j) {
                                PropertyItem a = arrpropertyItem[j];
                                prop.add(JSONUtils.parseProperty(a));
                            }
                        }
                        m.put("properts", prop);
                        modification.add(m);
                    }
                }
                i.put("properts", properts);
                i.put("modification", modification);
                jarray.add(i);
            }
            json.put("items", jarray);
        }
        return json.toString();
    }

    public static String parseItemInfo(Item item) {
        JSONObject obj = new JSONObject();
        obj.put("itemId", item.id);
        obj.put("count", item.count);
        return obj.toJSONString();
    }

    private static JSONObject parseProperty(PropertyItem item) {
        JSONObject h = new JSONObject();
        h.put("property", item.property.toString());
        h.put("value", item.value);
        return h;
    }

    public static String parseBattleMapList() {
        JSONObject json = new JSONObject();
        JSONArray jarray = new JSONArray();
        JSONArray jbattles = new JSONArray();
        for (Map map : MapsLoaderService.maps.values()) {
            JSONObject jmap = new JSONObject();
            jmap.put("id", map.id.replace(".xml", ""));
            jmap.put("name", map.name);
            jmap.put("gameName", "\u0442\u0438\u043f gameName");
            jmap.put("maxPeople", map.maxPlayers);
            jmap.put("maxRank", map.maxRank);
            jmap.put("minRank", map.minRank);
            jmap.put("themeName", map.themeId);
            jmap.put("skyboxId", map.skyboxId);
            jmap.put("tdm", map.tdm);
            jmap.put("ctf", map.ctf);
            jmap.put("dom", map.dom);
            jarray.add(jmap);
        }
        json.put("items", jarray);
        for (BattleInfo battle : battlesList.getList()) {
            jbattles.add(JSONUtils.parseBattleInfo(battle, 1));
        }
        json.put("battles", jbattles);
        return json.toString();
    }

    public static String parseBattleInfo(BattleInfo battle) {
        JSONObject json = new JSONObject();
        json.put("battleId", battle.battleId);
        json.put("mapId", battle.map.id);
        json.put("name", battle.name);
        json.put("previewId", String.valueOf(battle.map.id) + "_preview");
        json.put("team", battle.team);
        json.put("redPeople", battle.redPeople);
        json.put("bluePeople", battle.bluePeople);
        json.put("countPeople", battle.countPeople);
        json.put("maxPeople", battle.maxPeople);
        json.put("minRank", battle.minRank);
        json.put("maxRank", battle.maxRank);
        json.put("isPaid", battle.isPaid);
        return json.toJSONString();
    }

    public static JSONObject parseBattleInfo(BattleInfo battle, int i) {
        JSONObject json = new JSONObject();
        json.put("battleId", battle.battleId);
        json.put("mapId", battle.map.id);
        json.put("name", battle.name);
        json.put("previewId", String.valueOf(battle.map.id) + "_preview");
        json.put("team", battle.team);
        json.put("redPeople", battle.redPeople);
        json.put("bluePeople", battle.bluePeople);
        json.put("countPeople", battle.countPeople);
        json.put("maxPeople", battle.maxPeople);
        json.put("minRank", battle.minRank);
        json.put("maxRank", battle.maxRank);
        json.put("isPaid", battle.isPaid);
        return json;
    }

    public static String parseBattleInfoShow(BattleInfo battle, boolean spectator) {
        JSONObject json = new JSONObject();
        if (battle == null) {
            json.put("null_battle", true);
            return json.toJSONString();
        }
        try {
            JSONArray users = new JSONArray();
            if (battle != null && battle.model != null && battle.model.players != null) {
                JSONObject obj_user;
                for (BattlefieldPlayerController battlefieldPlayerController : battle.model.players.values()) {
                    obj_user = new JSONObject();
                    obj_user.put("nickname", battlefieldPlayerController.parentLobby.getLocalUser().getNickname());
                    obj_user.put("rank", battlefieldPlayerController.parentLobby.getLocalUser().getRang() + 1);
                    obj_user.put("kills", battlefieldPlayerController.statistic.getKills());
                    obj_user.put("team_type", battlefieldPlayerController.playerTeamType);
                    users.add(obj_user);
                }
                for (AutoEntryServices.Data data : autoEntryServices.getPlayersByBattle(battle.model)) {
                    obj_user = new JSONObject();
                    User user = databaseManager.getUserByNickName(data.userId);
                    obj_user.put("nickname", user.getNickname());
                    obj_user.put("rank", user.getRang() + 1);
                    obj_user.put("kills", data.statistic.getKills());
                    obj_user.put("team_type", data.teamType);
                    users.add(obj_user);
                }
            }
            json.put("users_in_battle", users);
            json.put("name", battle.name);
            json.put("maxPeople", battle.maxPeople);
            json.put("type", battle.battleType);
            json.put("battleId", battle.battleId);
            json.put("minRank", battle.minRank);
            json.put("maxRank", battle.maxRank);
            json.put("timeLimit", battle.time);
            json.put("timeCurrent", battle.model.getTimeLeft());
            json.put("killsLimt", battle.numKills);
            if (battle.battleType.equals("CTF")) {
                json.put("killsLimit", battle.numFlags);
            }
            if (battle.battleType.equals("DOM")) {
                json.put("killsLimit", battle.numFlags);
            }
            json.put("scoreRed", battle.scoreRed);
            json.put("scoreBlue", battle.scoreBlue);
            json.put("autobalance", battle.autobalance);
            json.put("friendlyFire", battle.friendlyFire);
            json.put("paidBattle", battle.isPaid);
            json.put("withoutBonuses", true);
            json.put("userAlreadyPaid", true);
            json.put("fullCash", true);
            json.put("spectator", spectator);
            json.put("previewId", String.valueOf(battle.map.id) + "_preview");
        } catch (Exception ex) {
            ex.printStackTrace();
            return json.toString();
        }
        return json.toJSONString();
    }

    public static String parseBattleModelInfo(BattleInfo battle, boolean spectatorMode) {
        JSONObject json = new JSONObject();
        json.put("kick_period_ms", 125000);
        json.put("map_id", battle.map.id.replace(".xml", ""));
        json.put("invisible_time", 3500);
        json.put("skybox_id", battle.map.skyboxId);
        json.put("spectator", spectatorMode);
        json.put("sound_id", battle.map.mapTheme.getAmbientSoundId());
        json.put("game_mode", battle.map.mapTheme.getGameModeId());
        json.put("equipmentChange", battle.equipmentChange);
        return json.toJSONString();
    }

    public static String parseTankData(BattlefieldModel player, BattlefieldPlayerController controller,
            Garage garageUser, Vector3 pos, boolean stateNull, String idTank, String nickname, int rank) {
        JSONObject json = new JSONObject();
        json.put("battleId", player.battleInfo.battleId);
        json.put("colormap_id", garageUser.mountColormap.id + "_m0");
        Optional<String> mountedHullSkin = SkinSystem.getInstance()
                .getMountedSkinForUserAndItem(garageUser.mountHull.id, controller.getUser().getId());
        json.put("hull_id", mountedHullSkin
                .orElse(garageUser.mountHull.id + "_m" + garageUser.mountHull.modificationIndex)); // Put hull skin id,
                                                                                                   // if its wasp_xt
                                                                                                   // otherwise wasp_m0
                                                                                                   // -> Not necessary
                                                                                                   // to send hull skin
                                                                                                   // separate since the
                                                                                                   // client doesnt
                                                                                                   // store special data
                                                                                                   // for it
        json.put("turret_id", (garageUser.mountTurret.id + "_m" + garageUser.mountTurret.modificationIndex)); // Put
                                                                                                              // default
                                                                                                              // turret
                                                                                                              // id
                                                                                                              // smoky_m0
                                                                                                              // -> Very
                                                                                                              // necessary
                                                                                                              // to send
                                                                                                              // the
                                                                                                              // actual
                                                                                                              // turret
                                                                                                              // id
                                                                                                              // since
                                                                                                              // the
                                                                                                              // server
                                                                                                              // sends
                                                                                                              // the
                                                                                                              // Shot
                                                                                                              // Data
                                                                                                              // and if
                                                                                                              // you
                                                                                                              // would
                                                                                                              // send
                                                                                                              // the
                                                                                                              // skin id
                                                                                                              // directly
                                                                                                              // it
                                                                                                              // would
                                                                                                              // try to
                                                                                                              // get
                                                                                                              // inexistent
                                                                                                              // shot
                                                                                                              // data
        Optional<String> mountedTurretSkin = SkinSystem.getInstance()
                .getMountedSkinForUserAndItem(garageUser.mountTurret.id, controller.getUser().getId());
        json.put("turret_skin", mountedTurretSkin.orElse(
                garageUser.mountTurret.id + "_m" + garageUser.mountTurret.modificationIndex)); // Replace with the
                                                                                               // turret skin smoky_xt
                                                                                               // or if it doesnt have
                                                                                               // skin put smoky_m +
                                                                                               // modification
        String shotEffectId = ShotEffectSystem.getInstance()
        .getMountedShotEffectForUserAndItem(garageUser.mountTurret.id, controller.getUser().getId())
        .orElse("");
        controller.tank.shotEffect = shotEffectId;
        controller.tank.hullSkin = mountedHullSkin.orElse(garageUser.mountHull.id + "_m" + garageUser.mountHull.modificationIndex);
        controller.tank.turretSkin = mountedTurretSkin.orElse(garageUser.mountTurret.id + "_m" + garageUser.mountTurret.modificationIndex);
        json.put("shot_effect", shotEffectId);
        json.put("team_type", controller.playerTeamType);
        if (pos == null) {
            pos = new Vector3(0.0f, 0.0f, 0.0f);
        }
        json.put("position", String.valueOf(pos.x) + "@" + pos.y + "@" + pos.z + "@" + pos.rot);
        json.put("tank_id", idTank);
        json.put("nickname", nickname);
        json.put("state", controller.tank.state);


        float microUpgradesSpeed = 0;
        float microUpgradesTurnSpeed = 0;
        float microUpgradesHealth = 0;
        for (final Item item : garageUser.items) {
            String selectedItremId = item.getId();
            if (selectedItremId.equals(StringUtils.concatStrings(garageUser.mountHull.id, "_m",
                    String.valueOf(garageUser.mountHull.modificationIndex)))) {
                int microUpgrades = garageUser.mountHull.microUpgrades;
                int microUpgradesMax = 10;
                String itemId = StringUtils.concatStrings(item.id, "_m", Integer.toString(item.modificationIndex));

                float currentSpeed = HullsFactory.getInstance()
                        .getHull(garageUser.mountHull.id + "_m" + garageUser.mountHull.modificationIndex).speed;
                float nextModificationSpeed;
                float nextUpgradeSpeedDifference;

                float currentTurnSpeed = HullsFactory.getInstance()
                        .getHull(garageUser.mountHull.id + "_m" + garageUser.mountHull.modificationIndex).turnSpeed;
                float nextModificationTurnSpeed;
                float nextUpgradeTurnSpeedDifference;

                float currentHealth = Math.abs(HullsFactory.getInstance()
                        .getHull(garageUser.mountHull.id + "_m" + garageUser.mountHull.modificationIndex).hp);
                float nextModificationHealth;
                float nextUpgradeHealhDifference;

                if (item.modificationIndex == 3) {
                    String itemIdMinusMod = itemId.replace("m3", "m2");
                    nextModificationSpeed = HullsFactory.getInstance().getHull(itemIdMinusMod).speed;
                    nextUpgradeSpeedDifference = currentSpeed - nextModificationSpeed;

                    nextModificationTurnSpeed = HullsFactory.getInstance().getHull(itemIdMinusMod).turnSpeed;
                    nextUpgradeTurnSpeedDifference = currentTurnSpeed - nextModificationTurnSpeed;

                    nextModificationHealth = HullsFactory.getInstance().getHull(itemIdMinusMod).hp;
                    nextUpgradeHealhDifference = currentHealth - nextModificationHealth;
                } else {
                    String[] itemData = itemId.split("_m");
                    String nextItemModification = StringUtils.concatStrings(itemData[0], "_m",
                            String.valueOf(Integer.parseInt(itemData[1]) + 1));
                    nextModificationSpeed = HullsFactory.getInstance().getHull(nextItemModification).speed;
                    nextUpgradeSpeedDifference = nextModificationSpeed - currentSpeed;

                    nextModificationTurnSpeed = HullsFactory.getInstance().getHull(nextItemModification).turnSpeed;
                    nextUpgradeTurnSpeedDifference = nextModificationTurnSpeed - currentTurnSpeed;

                    nextModificationHealth = HullsFactory.getInstance().getHull(nextItemModification).hp;
                    nextUpgradeHealhDifference = nextModificationHealth - currentHealth;
                }
                if (microUpgrades == 0) {
                    microUpgradesSpeed = 0;
                    microUpgradesTurnSpeed = 0;
                    microUpgradesHealth = 0;
                } else {
                    microUpgradesSpeed = TankKillModel.calculateNextUpgrade(nextUpgradeSpeedDifference,
                            microUpgrades - 1, microUpgradesMax);
                    microUpgradesTurnSpeed = TankKillModel.calculateNextUpgrade(nextUpgradeTurnSpeedDifference,
                            microUpgrades - 1, microUpgradesMax);
                    microUpgradesHealth = TankKillModel.calculateNextUpgrade(nextUpgradeHealhDifference,
                            microUpgrades - 1, microUpgradesMax);
                }
            }
        }
        if (!controller.battle.battleInfo.microUpgrades) {
            microUpgradesSpeed = 0;
            microUpgradesTurnSpeed = 0;
            microUpgradesHealth = 0;
        }
        controller.tank.maxHp = controller.tank.getHullInfo().hp;
        controller.tank.setMicroUpgradesHealth(microUpgradesHealth);
        json.put("turn_speed", Float.valueOf(controller.tank.getHullInfo().turnSpeed + microUpgradesTurnSpeed));
        json.put("speed", Float.valueOf(controller.tank.getHullInfo().speed + microUpgradesSpeed));
        json.put("turret_turn_speed", Float.valueOf(controller.tank.turretRotationSpeed));
        json.put("health", controller.tank.healthPoints);
        json.put("rank", rank + 1);
        json.put("mass", Float.valueOf(controller.tank.getHullInfo().mass));
        json.put("power", Float.valueOf(controller.tank.getHullInfo().power));
        json.put("kickback", Float.valueOf(controller.tank.getWeaponInfo().getEntity().getShotData().kickback));
        json.put("turret_rotation_accel",
                Float.valueOf(controller.tank.getWeaponInfo().getEntity().getShotData().turretRotationAccel));
        json.put("impact_force", Float.valueOf(controller.tank.getWeaponInfo().getEntity().getShotData().impactCoeff));
        json.put("state_null", stateNull);
        json.put("isPremium", PremiumService.getInstance().getPremiumTime(controller.getUser().getId()).isActivated());
        return json.toJSONString();
    }

    public static String parseMoveCommand(BattlefieldPlayerController player) {
        Tank tank = player.tank;
        JSONObject json = new JSONObject();
        JSONObject pos = new JSONObject();
        JSONObject orient = new JSONObject();
        JSONObject line = new JSONObject();
        JSONObject angle = new JSONObject();
        pos.put("x", Float.valueOf(tank.position.x));
        pos.put("y", Float.valueOf(tank.position.y));
        pos.put("z", Float.valueOf(tank.position.z));
        orient.put("x", Float.valueOf(tank.orientation.x));
        orient.put("y", Float.valueOf(tank.orientation.y));
        orient.put("z", Float.valueOf(tank.orientation.z));
        line.put("x", Float.valueOf(tank.linVel.x));
        line.put("y", Float.valueOf(tank.linVel.y));
        line.put("z", Float.valueOf(tank.linVel.z));
        angle.put("x", Float.valueOf(tank.angVel.x));
        angle.put("y", Float.valueOf(tank.angVel.y));
        angle.put("z", Float.valueOf(tank.angVel.z));
        json.put("position", pos);
        json.put("orient", orient);
        json.put("line", line);
        json.put("angle", angle);
        json.put("turretDir", tank.turretDir);
        json.put("ctrlBits", tank.controllBits);
        json.put("tank_id", tank.id);
        return json.toJSONString();
    }

    public static String parseBattleChatMessage(BattleChatMessage msg) {
        JSONObject jobj = new JSONObject();
        jobj.put("userId", msg.userId);
        jobj.put("nickname", msg.nickname);
        jobj.put("isPremium", msg.isPremium);
        jobj.put("rank", msg.rank + 1);
        jobj.put("message", msg.message);
        jobj.put("team_type", msg.teamType);
        jobj.put("system", msg.system);
        jobj.put("team", msg.team);
        return jobj.toJSONString();
    }

    public static String parseBonusInfo(Bonus bonus, int inc, int disappearingTime) {
        JSONObject jobj = new JSONObject();
        jobj.put("id", String.valueOf(bonus.type.toString()) + "_" + inc);
        jobj.put("x", Float.valueOf(bonus.position.x));
        jobj.put("y", Float.valueOf(bonus.position.y));
        jobj.put("z", Float.valueOf(bonus.position.z));
        jobj.put("disappearing_time", disappearingTime);
        return jobj.toJSONString();
    }

    public static String parseBonusInfoOnJoin(Bonus bonus, int inc, int disappearingTime) {
        JSONObject jobj = new JSONObject();
        jobj.put("id", String.valueOf(bonus.type.toString()) + "_" + inc);
        jobj.put("x", Float.valueOf(bonus.position.x));
        jobj.put("y", Float.valueOf(bonus.position.y));
        jobj.put("z", Float.valueOf(bonus.position.z));
        jobj.put("disappearing_time", disappearingTime);
        return jobj.toJSONString();
    }

    public static JSONObject parseSpecialEntity(IEntity entity) {
        JSONObject j = new JSONObject();
        switch (entity.getType()) {
            case FLAMETHROWER: {
                FlamethrowerEntity fm = (FlamethrowerEntity) entity;
                j.put("cooling_speed", fm.coolingSpeed);
                j.put("cone_angle", Float.valueOf(fm.coneAngle));
                j.put("heating_speed", fm.heatingSpeed);
                j.put("heat_limit", fm.heatLimit);
                j.put("range", Float.valueOf(fm.range));
                j.put("target_detection_interval", fm.targetDetectionInterval);
                break;
            }
            case TWINS: {
                TwinsEntity te = (TwinsEntity) entity;
                j.put("shot_radius", Float.valueOf(te.shotRadius));
                j.put("shot_range", Float.valueOf(te.shotRange));
                j.put("shot_speed", Float.valueOf(te.shotSpeed));
                break;
            }
            case ISIDA: {
                IsidaEntity ie = (IsidaEntity) entity;
                j.put("angle", Float.valueOf(ie.maxAngle));
                j.put("capacity", ie.capacity);
                j.put("chargeRate", ie.chargeRate);
                j.put("tickPeriod", ie.tickPeriod);
                j.put("coneAngle", Float.valueOf(ie.lockAngle));
                j.put("dischargeRate", ie.dischargeRate);
                j.put("radius", Float.valueOf(ie.maxRadius));
                break;
            }
            case THUNDER: {
                ThunderEntity the = (ThunderEntity) entity;
                j.put("impactForce", Float.valueOf(the.impactForce));
                j.put("maxSplashDamageRadius", Float.valueOf(the.maxSplashDamageRadius));
                j.put("minSplashDamagePercent", Float.valueOf(the.minSplashDamagePercent));
                j.put("minSplashDamageRadius", Float.valueOf(the.minSplashDamageRadius));
                break;
            }
            case FREZZE: {
                FrezeeEntity frezeeEntity = (FrezeeEntity) entity;
                j.put("damageAreaConeAngle", Float.valueOf(frezeeEntity.damageAreaConeAngle));
                j.put("damageAreaRange", Float.valueOf(frezeeEntity.damageAreaRange));
                j.put("energyCapacity", frezeeEntity.energyCapacity);
                j.put("energyRechargeSpeed", frezeeEntity.energyRechargeSpeed);
                j.put("energyDischargeSpeed", frezeeEntity.energyDischargeSpeed);
                j.put("weaponTickMsec", frezeeEntity.weaponTickMsec);
                break;
            }
            case RICOCHET: {
                RicochetEntity ricochetEntity = (RicochetEntity) entity;
                j.put("energyCapacity", ricochetEntity.energyCapacity);
                j.put("energyPerShot", ricochetEntity.energyPerShot);
                j.put("energyRechargeSpeed", Float.valueOf(ricochetEntity.energyRechargeSpeed));
                j.put("shotDistance", Float.valueOf(ricochetEntity.shotDistance));
                j.put("shotRadius", Float.valueOf(ricochetEntity.shotRadius));
                j.put("shotSpeed", Float.valueOf(ricochetEntity.shotSpeed));
                break;
            }
            case SHAFT: {
                ShaftEntity se = (ShaftEntity) entity;
                j.put("max_energy", Float.valueOf(se.maxEnergy));
                j.put("charge_rate", Float.valueOf(se.chargeRate));
                j.put("discharge_rate", Float.valueOf(se.dischargeRate));
                j.put("elevation_angle_up", Float.valueOf(se.elevationAngleUp));
                j.put("elevation_angle_down", Float.valueOf(se.elevationAngleDown));
                j.put("vertical_targeting_speed", Float.valueOf(se.verticalTargetingSpeed));
                j.put("horizontal_targeting_speed", Float.valueOf(se.horizontalTargetingSpeed));
                j.put("inital_fov", Float.valueOf(se.initialFOV));
                j.put("minimum_fov", Float.valueOf(se.minimumFOV));
                j.put("shrubs_hiding_radius_min", Float.valueOf(se.shrubsHidingRadiusMin));
                j.put("shrubs_hiding_radius_max", Float.valueOf(se.shrubsHidingRadiusMax));
            }
        }
        return j;
    }

    public static String parseWeapons(Collection<IEntity> weapons, HashMap<String, WeaponWeakeningData> wwds) {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (IEntity entity : weapons) {
            JSONObject weapon = new JSONObject();
            WeaponWeakeningData wwd = wwds.get(entity.getShotData().id);
            weapon.put("auto_aiming_down", entity.getShotData().autoAimingAngleDown);
            weapon.put("auto_aiming_up", entity.getShotData().autoAimingAngleUp);
            weapon.put("num_rays_down", entity.getShotData().numRaysDown);
            weapon.put("num_rays_up", entity.getShotData().numRaysUp);
            weapon.put("reload", entity.getShotData().reloadMsec);
            weapon.put("id", entity.getShotData().id);
            if (wwd != null) {
                weapon.put("max_damage_radius", wwd.maximumDamageRadius);
                weapon.put("min_damage_radius", wwd.minimumDamageRadius);
                weapon.put("min_damage_percent", wwd.minimumDamagePercent);
                weapon.put("has_wwd", true);
            } else {
                weapon.put("has_wwd", false);
            }
            weapon.put("special_entity", JSONUtils.parseSpecialEntity(entity));
            array.add(weapon);
        }
        obj.put("weapons", array);
        return obj.toJSONString();
    }

    public static String parseTankSpec(Tank tank, boolean notSmooth) {
        JSONObject obj = new JSONObject();
        obj.put("speed", Float.valueOf(tank.speed));
        obj.put("turnSpeed", Float.valueOf(tank.turnSpeed));
        obj.put("turretRotationSpeed", Float.valueOf(tank.turretRotationSpeed));
        obj.put("immediate", notSmooth);
        return obj.toString();
    }

    public static String boolToString(boolean src) {
        return src ? "true" : "false";
    }

    @SneakyThrows
    public static <T> T parse(String message, Class<T> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message);
        Constructor<T> simpleConstructor = clazz.getConstructor();
        simpleConstructor.setAccessible(true);
        T returnedObject = simpleConstructor.newInstance();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            field.set(returnedObject, jsonObject.get(field.getName()));
        }

        return returnedObject;
    }
}
