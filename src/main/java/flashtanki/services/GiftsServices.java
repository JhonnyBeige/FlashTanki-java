/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package flashtanki.services;

import flashtanki.commands.Type;
import flashtanki.users.garage.containers.ContainerSystem;
import flashtanki.json.ContainerItemsFactory;
import flashtanki.lobby.LobbyManager;
import flashtanki.lobby.shop.GiveItemService;
import flashtanki.main.database.DatabaseManager;
import flashtanki.main.database.impl.DatabaseManagerImpl;
import flashtanki.services.annotations.ServicesInject;
import flashtanki.users.User;
import flashtanki.users.garage.GarageItemsLoader;
import flashtanki.users.garage.enums.ItemType;
import flashtanki.users.garage.items.Item;

import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GiftsServices {
    private static final GiftsServices INSTANCE = new GiftsServices();
    @ServicesInject(target=DatabaseManagerImpl.class)
    private final DatabaseManager database = DatabaseManagerImpl.instance();
    private final List<String> loadedGifts = new ArrayList<String>(Collections.singletonList(ContainerItemsFactory.getData()));

    public static GiftsServices instance() {
        return INSTANCE;
    }

    public void userOnGiftsWindowOpen(LobbyManager lobby) {
        String userContainers = ContainerSystem.getInstance()
                    .getUserContainersResponse(lobby.getLocalUser().getId());
        JSONParser parser = new JSONParser();
        try{
            JSONObject jsonObject = (JSONObject) parser.parse(userContainers);
            JSONArray list = (JSONArray) jsonObject.get("list");
            JSONObject firstContainer = (JSONObject) list.get(0);
            long count = (long) firstContainer.get("count");
            
            lobby.send(Type.LOBBY, "show_gifts_window", String.valueOf(this.loadedGifts), Long.toString(count));
        }catch(ParseException e){

        }
    }

    public void tryRollItem(LobbyManager lobby) {
        Object itemName;
        String itemId = null;
        int countItems = 0;
        int rarity = 0;
        int offsetCrystalls = 0;
        try {
            JSONArray jsonArray = GiftsServices.parseJsonArray(String.valueOf(this.loadedGifts));
            JSONObject randomItem = GiftsServices.pickRandomItem(jsonArray);
            itemId = (String)randomItem.get("item_id");
            rarity = ((Long)randomItem.get("rarity")).intValue();
            if(!itemId.equals("health") && !itemId.equals("armor") && !itemId.equals("damage") && !itemId.equals("n2o") && !itemId.equals("mine")){
                while(lobby.getLocalUser().getGarage().containsItem(itemId)){
                    randomItem = GiftsServices.pickRandomItem(jsonArray, rarity);
                    itemId = (String)randomItem.get("item_id");
                }
            }
            countItems = ((Long)randomItem.get("count")).intValue();
            ContainerSystem.getInstance().openContainer(lobby.getLocalUser(), "container_0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (itemId.startsWith("premium")) {
            if(countItems == 1){
                itemName = countItems + " Day of Premium";
            }else{
                itemName = countItems + " Days of Premium";
            }
            User giveToUser = lobby.getLocalUser();
            GiveItemService giveItemService = GiveItemService.getInstance();
            long giveUserId = giveToUser.getId();
            String giveItemId = "premium";
            int giveItemCount = countItems * 86000;
            String jsonRequest = "{\"userId\":"+giveUserId+",\"itemId\":\""+giveItemId+"\",\"count\":"+giveItemCount+"}";
            giveItemService.onReceive(jsonRequest);
        }else if (itemId.startsWith("set_")) {
            String countString = itemId.substring(4);
            int setItemCount = Integer.parseInt(countString);
            itemName = "Set " + setItemCount;
            this.addBonusItemsToGarage(lobby.getLocalUser(), setItemCount);
        } else if (itemId.equals("crystalls")) {
            itemName = "Crystalls x" + countItems;
            lobby.addCrystall(countItems);
            offsetCrystalls = countItems;
        } else {
            itemName = this.getItemNameWithCount(lobby, itemId, countItems);
            offsetCrystalls = 0;//this.getOffsetCrystalls(lobby, GarageItemsLoader.getInstance().items.get(itemId));
            if(GarageItemsLoader.getInstance().items.get(itemId) == null){
                System.out.println(itemId + " is null");
            }
            this.rewardGiftItemToUser(lobby, GarageItemsLoader.getInstance().items.get(itemId), countItems);
        }
        Optional<Item> item = lobby.getLocalUser().getGarage().getItemById("gift");
        this.updateInventory(lobby, item, 1);
        lobby.send(Type.LOBBY, "item_rolled", itemId, countItems + ";" + offsetCrystalls, itemName + ";" + rarity);
    }

    public void rollItems(LobbyManager lobby, int rollCount) {
        JSONArray jsonArrayGift = new JSONArray();
        int offsetCrystalls = 0;
        StringBuilder resultLogs = new StringBuilder("[GIFT_SYSTEM_LOG_OUT]: Details:");
        resultLogs.append(" Nickname: ").append(lobby.getLocalUser().getNickname());
        resultLogs.append(" gifts opened count: ").append(rollCount).append("\n");
        try {
            JSONArray jsonArray = GiftsServices.parseJsonArray(String.valueOf(this.loadedGifts));
            for (int i = 0; i < rollCount; ++i) {
                ContainerSystem.getInstance().openContainer(lobby.getLocalUser(), "container_0");
                Object itemName;
                JSONObject randomItem = GiftsServices.pickRandomItem(jsonArray);
                String itemId = (String)randomItem.get("item_id");
                int rarity = ((Long)randomItem.get("rarity")).intValue();

                if(!itemId.equals("health") && !itemId.equals("armor") && !itemId.equals("damage") && !itemId.equals("n2o") && !itemId.equals("mine")){
                    while(lobby.getLocalUser().getGarage().containsItem(itemId)){
                        randomItem = GiftsServices.pickRandomItem(jsonArray, rarity);
                        itemId = (String)randomItem.get("item_id");
                    }
                }

                int countItems = ((Long)randomItem.get("count")).intValue();
                JSONArray numInventoryCounts = new JSONArray();
                for (int j = 0; j < 5; ++j) {
                    numInventoryCounts.add(0);
                }
                if (itemId.startsWith("premium")){
                    if(countItems == 1){
                        itemName = countItems + " Day of Premium";
                    }else{
                        itemName = countItems + " Days of Premium";
                    }
                    User giveToUser = lobby.getLocalUser();
                    GiveItemService giveItemService = GiveItemService.getInstance();
                    long giveUserId = giveToUser.getId();
                    String giveItemId = "premium";
                    int giveItemCount = countItems * 86000;
                    String jsonRequest = "{\"userId\":"+giveUserId+",\"itemId\":\""+giveItemId+"\",\"count\":"+giveItemCount+"}";
                    giveItemService.onReceive(jsonRequest);
                }else if (itemId.startsWith("set_")) {
                    String countString = itemId.substring(4);
                    int setItemCount = Integer.parseInt(countString);
                    itemName = "Set" + setItemCount;
                    this.addBonusItemsToGarage(lobby.getLocalUser(), setItemCount);
                } else if (itemId.equals("crystalls")) {
                    itemName = "Crystalls x" + countItems;
                    lobby.addCrystall(countItems);
                } else {
                    itemName = this.getItemNameWithCount(lobby, itemId, countItems);
                    offsetCrystalls = 0;//this.getOffsetCrystalls(lobby, GarageItemsLoader.getInstance().items.get(itemId));
                    if(GarageItemsLoader.getInstance().items.get(itemId) == null){
                        System.out.println(itemId + " is null");
                    }
                    this.rewardGiftItemToUser(lobby, GarageItemsLoader.getInstance().items.get(itemId), countItems);
                }
                resultLogs.append("===================================\n");
                resultLogs.append("[GIFT_SYSTEM_LOG_OUT]: Prize: ").append((String)itemName).append("\n");
                JSONObject newItem = new JSONObject();
                newItem.put("itemId", itemId);
                newItem.put("visualItemName", itemName);
                newItem.put("rarity", rarity);
                newItem.put("offsetCrystalls", offsetCrystalls);
                newItem.put("numInventoryCounts", numInventoryCounts);
                jsonArrayGift.add(newItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Optional<Item> item = lobby.getLocalUser().getGarage().getItemById("gift");
        this.updateInventory(lobby, item, rollCount);
        lobby.send(Type.LOBBY, "items_rolled", String.valueOf(jsonArrayGift));
    }

    private void updateInventory(LobbyManager lobby, Optional<Item> item, int amountToRemove) {
        // Item defaulItem = null;
        // String itemId = GarageItemsLoader.getInstance().items.getOrDefault(item, defaulItem).id;
        // lobby.getLocalUser().getGarage().giveItem(itemId, -amountToRemove, null, null);
        // if (defaulItem.count <= 0) {
        //     lobby.getLocalUser().getGarage().items.remove(item);
        // }
        // lobby.getLocalUser().getGarage().parseJSONData();
        // this.database.update(lobby.getLocalUser().getGarage());
    }

    private String getItemNameWithCount(LobbyManager lobby, String itemId, int countItems) {
        if(GarageItemsLoader.getInstance().items.get(itemId) == null){
            return itemId;
        }
        String itemName = GarageItemsLoader.getInstance().items.get(itemId).name.localizatedString(lobby.getLocalUser().getLocalization());
        List<String> specialItemIds = Arrays.asList("mine", "n2o", "health", "armor", "double_damage");
        if (specialItemIds.contains(itemId)) {
            Optional<Item> bonusItem = lobby.getLocalUser().getGarage().getItemById(itemId);
            if (bonusItem == null) {
                lobby.getLocalUser().getGarage().giveItem(itemId, countItems, () -> {}, () -> {});
            }
            itemName = itemName + " x" + countItems;
        }
        return itemName;
    }

    private void rewardGiftItemToUser(LobbyManager lobby, Item item) {
        boolean containsItem = lobby.getLocalUser().getGarage().containsItem(item.id);
        if (containsItem) {
            if (item.itemType != ItemType.INVENTORY) {
                lobby.addCrystall(item.price / 2);
            }else{
                lobby.getLocalUser().getGarage().giveItem(item.id + "_m" + item.modificationIndex, 1, () -> {}, () -> {});
            }
        } else {
            lobby.getLocalUser().getGarage().giveItem(item.id + "_m" + item.modificationIndex, 1, () -> {}, () -> {});
        }
    }

    private void rewardGiftItemToUser(LobbyManager lobby, Item item, int count) {
        boolean containsItem = lobby.getLocalUser().getGarage().containsItem(item.id);
        if (containsItem) {
            if (item.itemType != ItemType.INVENTORY) {
                lobby.addCrystall(item.price / 2);
            }else{
                lobby.getLocalUser().getGarage().giveItem(item.id + "_m" + item.modificationIndex, count, () -> {}, () -> {});
            }
        } else {
            lobby.getLocalUser().getGarage().giveItem(item.id + "_m" + item.modificationIndex, count, () -> {}, () -> {});
        }
    }

    private int getOffsetCrystalls(LobbyManager lobby, Item item) {
        boolean containsItem = lobby.getLocalUser().getGarage().containsItem(item.id);
        int offsetCrystalls = 0;
        if (containsItem && item.itemType != ItemType.INVENTORY) {
            offsetCrystalls = item.price / 2;
        }
        return offsetCrystalls;
    }

    public static JSONArray parseJsonArray(String jsonArrayString) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        return (JSONArray)jsonParser.parse(jsonArrayString);
    }

    private void addBonusItemsToGarage(User localUser, int setItemCount) {
        List<String> bonusItemIds = Arrays.asList("n2o", "double_damage", "armor", "mine", "health");
        for (String bonusItemId : bonusItemIds) {
            Optional<Item> bonusItem = localUser.getGarage().getItemById(bonusItemId);
            if (bonusItem == null) {
                localUser.getGarage().giveItem(bonusItemId, setItemCount, () -> {}, () -> {});
            }
        }
    }

    public static JSONObject pickRandomItem(JSONArray jsonArray) {
        Random random = new Random();
        double[] rarityProbabilities = {50.0, 34.0, 10.0, 5.0, 0.3};
        double totalProbabilitySum = 0;
        for (double probability : rarityProbabilities) {
            totalProbabilitySum += probability;
        }
        
        double randomValue = random.nextDouble() * totalProbabilitySum;
        double cumulativeProbability = 0;
        int rarity = 0;
        for (int i = 0; i < rarityProbabilities.length; i++) {
            cumulativeProbability += rarityProbabilities[i];
            if (randomValue < cumulativeProbability) {
                rarity = i;
                break;
            }
        }
        
        ArrayList<JSONObject> itemsWithRarity = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject item = (JSONObject) jsonArray.get(i);
            int itemRarity = ((Long) item.get("rarity")).intValue();
            if (itemRarity == rarity) {
                itemsWithRarity.add(item);
            }
        }
        
        if (itemsWithRarity.size() > 0) {
            int randomIndex = random.nextInt(itemsWithRarity.size());
            return itemsWithRarity.get(randomIndex);
        }
        return (JSONObject) jsonArray.get(0);
    }
    
    public static JSONObject pickRandomItem(JSONArray jsonArray, int rarity) {
        Random random = new Random();
        ArrayList<JSONObject> itemsWithRarity = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.size(); ++i) {
            JSONObject item = (JSONObject)jsonArray.get(i);
            int itemRarity = ((Long)item.get("rarity")).intValue();
            if (itemRarity != rarity) continue;
            itemsWithRarity.add(item);
        }
        if (itemsWithRarity.size() > 0) {
            int randomIndex = random.nextInt(itemsWithRarity.size());
            return itemsWithRarity.get(randomIndex);
        }
        return (JSONObject)jsonArray.get(0);
    }
}

