package flashtanki.skin;

import com.fasterxml.jackson.databind.ObjectMapper;
import flashtanki.commands.Type;
import flashtanki.lobby.LobbyManager;
import flashtanki.services.LobbysServices;
import flashtanki.skin.list.SkinItem;
import flashtanki.skin.list.SkinItemRepository;
import flashtanki.skin.user.UserSkin;
import flashtanki.skin.user.UserSkinRepository;
import flashtanki.system.localization.Localization;
import flashtanki.users.User;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SkinSystem {
    private static SkinSystem instance;
    private final SkinItemRepository skinItemRepository;
    private final UserSkinRepository userSkinRepository;

    public static SkinSystem getInstance() {
        if (SkinSystem.instance == null) {
            SkinSystem.instance = new SkinSystem();
        }
        return SkinSystem.instance;
    }

    private SkinSystem() {
        this.skinItemRepository = SkinItemRepository.getInstance();
        this.userSkinRepository = UserSkinRepository.getInstance();
    }

    public void buySkin(Long userId, String skinClientId) {
        LobbyManager lobbyByUserId = Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                .orElseThrow(() -> new RuntimeException("Lobby not found for user " + userId));

        SkinItem skinToBuy = skinItemRepository.findSkinByClientId(skinClientId).orElseThrow(
                () -> new RuntimeException("Skin not found " + skinClientId));

        User user = lobbyByUserId.getLocalUser();
        int userCrystals = user.getCrystall();
        if (userCrystals < skinToBuy.getPrice()) {
            return;
        }
        giveSkin(userId, skinClientId, () -> {
            lobbyByUserId.addCrystall(-skinToBuy.getPrice());
            lobbyByUserId.send(Type.GARAGE, "update_skins_for_item",
                    getSkinsForItem(skinToBuy.getItemId(), userId, user.getLocalization()));
        }, () -> {

        });
    }

    public void giveSkin(Long userId, String skinClientId, Runnable successCallback, Runnable failCallback) {
        SkinItem skinToGive = skinItemRepository.findSkinByClientId(skinClientId).orElseThrow(
                () -> new RuntimeException("Skin not found " + skinClientId));
        if (userSkinRepository.userHaveSkin(userId, skinClientId)) {
            failCallback.run();
            return;
        }
        userSkinRepository.save(UserSkin.builder()
                .userId(userId)
                .skinId(skinToGive.getId())
                .mounted(false)
                .build());
        successCallback.run();
    }

    public void unmountAllSkinsByItem(Long userId, String item, boolean needCallback) {
        userSkinRepository.unmountAllSkinsByItem(userId, item);
        LobbysServices.getInstance().getLobbyByUserId(userId).send(Type.GARAGE, "update_skins_for_item",
                getSkinsForItem(item, userId,
                        LobbysServices.getInstance().getLobbyByUserId(userId).getLocalUser().getLocalization()));
        if (needCallback) {
            LobbysServices.getInstance().getLobbyByUserId(userId).send(Type.GARAGE, "mount_skin", item, "");
        }
    }

    public Map<Long, UserSkin> getMountedUserSkins(Long userId) {
        return userSkinRepository.getMountedUserSkins(userId).stream()
                .collect(Collectors.toMap(UserSkin::getSkinId, userSkin -> userSkin));
    }

    public List<UserSkin> getAllUserSkins(Long userId) {
        return userSkinRepository.getAllUserSkins(userId);
    }

    public Map<Long, SkinItem> getAllSkins() {
        return skinItemRepository.findAll().stream()
                .collect(Collectors.toMap(SkinItem::getId, skinItem -> skinItem));
    }

    @SneakyThrows
    public String getSkinsForItem(String itemId, Long userId, Localization localization) {
        Map<Long, UserSkin> userSkins = userSkinRepository.getUserSkinsForItem(userId, itemId).stream()
                .collect(Collectors.toMap(UserSkin::getSkinId, userSkin -> userSkin));
        List<SkinItem> itemSkins = skinItemRepository.getItemSkins(itemId);
        List<ItemSkinsObject> response = itemSkins.stream()
                .map(skinItem -> ItemSkinsObject.builder()
                        .item(skinItem.getItemId())
                        .title(localization == Localization.RU ? skinItem.getTitleRu() : skinItem.getTitleEn())
                        .desc(localization == Localization.RU ? skinItem.getDescRu() : skinItem.getDescEn())
                        .price(skinItem.getPrice())
                        .skin(skinItem.getClientId())
                        .bought(userSkins.containsKey(skinItem.getId()))
                        .equipped(
                                userSkins.containsKey(skinItem.getId()) && userSkins.get(skinItem.getId()).isMounted())
                        .build())
                .collect(Collectors.toList());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(response);
    }

    public void equipSkin(long userId, String skinClientId, boolean needCallback) {
        if (userSkinRepository.userHaveSkin(userId, skinClientId)) {
            Optional<SkinItem> skinItem = skinItemRepository.findSkinByClientId(skinClientId);
            userSkinRepository.unmountAllSkinsByItem(userId, skinItem.get().getItemId());
            userSkinRepository.mountSkin(userId, skinItem.get().getId());
            Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                    .ifPresent(lobbyManager -> {
                        lobbyManager.send(Type.GARAGE, "update_skins_for_item",
                                getSkinsForItem(skinItem.get().getItemId(),
                                        userId,
                                        lobbyManager.getLocalUser().getLocalization()));
                        if (needCallback) {
                            lobbyManager.send(Type.GARAGE, "mount_skin", skinItem.get().getItemId(), skinClientId);
                        }
                    });
        }
    }

    public Optional<String> getMountedSkinForUserAndItem(String itemId, long userId) {
        return userSkinRepository.getMountedUserSkin(userId, itemId)
                .map(SkinItem::getClientId);

    }

    public boolean existSkin(String clientId) {
        return skinItemRepository.findSkinByClientId(clientId).isPresent();
    }
}
