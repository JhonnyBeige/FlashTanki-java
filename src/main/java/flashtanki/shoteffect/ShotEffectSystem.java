package flashtanki.shoteffect;

import com.fasterxml.jackson.databind.ObjectMapper;
import flashtanki.commands.Type;
import flashtanki.lobby.LobbyManager;
import flashtanki.services.LobbysServices;
import flashtanki.shoteffect.list.ShotEffectItem;
import flashtanki.shoteffect.list.ShotEffectItemRepository;
import flashtanki.shoteffect.user.UserShotEffect;
import flashtanki.shoteffect.user.UserShotEffectRepository;
import flashtanki.system.localization.Localization;
import flashtanki.users.User;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShotEffectSystem {
    private static ShotEffectSystem instance;
    private final ShotEffectItemRepository shotEffectItemRepository;
    private final UserShotEffectRepository userShotEffectRepository;

    public static ShotEffectSystem getInstance() {
        if (ShotEffectSystem.instance == null) {
            ShotEffectSystem.instance = new ShotEffectSystem();
        }
        return ShotEffectSystem.instance;
    }

    private ShotEffectSystem() {
        this.shotEffectItemRepository = ShotEffectItemRepository.getInstance();
        this.userShotEffectRepository = UserShotEffectRepository.getInstance();
    }

    public void buyShotEffect(Long userId, String ShotEffectClientId) {
        LobbyManager lobbyByUserId = Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                .orElseThrow(() -> new RuntimeException("Lobby not found for user " + userId));

        ShotEffectItem ShotEffectToBuy = shotEffectItemRepository.findShotEffectByClientId(ShotEffectClientId)
                .orElseThrow(
                        () -> new RuntimeException("ShotEffect not found " + ShotEffectClientId));

        User user = lobbyByUserId.getLocalUser();
        int userCrystals = user.getCrystall();
        if (userCrystals < ShotEffectToBuy.getPrice()) {
            return;
        }
        giveShotEffect(userId, ShotEffectClientId, () -> {
            lobbyByUserId.addCrystall(-ShotEffectToBuy.getPrice());
            lobbyByUserId.send(Type.GARAGE, "update_shot_effects_for_item",
                    getShotEffectsForItem(ShotEffectToBuy.getItemId(), userId, user.getLocalization()));
        }, () -> {

        });
    }

    public void giveShotEffect(Long userId, String ShotEffectClientId, Runnable successCallback,
            Runnable failCallback) {
        ShotEffectItem ShotEffectToGive = shotEffectItemRepository.findShotEffectByClientId(ShotEffectClientId)
                .orElseThrow(
                        () -> new RuntimeException("ShotEffect not found " + ShotEffectClientId));
        if (userShotEffectRepository.userHaveShotEffect(userId, ShotEffectClientId)) {
            failCallback.run();
            return;
        }
        userShotEffectRepository.save(UserShotEffect.builder()
                .userId(userId)
                .shotEffectId(ShotEffectToGive.getId())
                .mounted(false)
                .build());
        successCallback.run();
    }

    public void unmountAllShotEffectsByItem(Long userId, String item, boolean needCallback) {
        userShotEffectRepository.unmountAllShotEffectsByItem(userId, item);
        LobbysServices.getInstance().getLobbyByUserId(userId).send(Type.GARAGE, "update_shot_effects_for_item",
                getShotEffectsForItem(item, userId,
                        LobbysServices.getInstance().getLobbyByUserId(userId).getLocalUser().getLocalization()));
        if (needCallback) {
            LobbysServices.getInstance().getLobbyByUserId(userId).send(Type.GARAGE, "mount_shot_effect", item, "");
        }

    }

    public Map<Long, UserShotEffect> getMountedUserShotEffects(Long userId) {
        return userShotEffectRepository.getMountedUserShotEffects(userId).stream()
                .collect(Collectors.toMap(UserShotEffect::getShotEffectId, userShotEffect -> userShotEffect));
    }

    public List<UserShotEffect> getAllUserShotEffects(Long userId) {
        return userShotEffectRepository.getAllUserShotEffects(userId);
    }

    public Map<Long, ShotEffectItem> getAllShotEffects() {
        return shotEffectItemRepository.findAll().stream()
                .collect(Collectors.toMap(ShotEffectItem::getId, shotEffectItem -> shotEffectItem));
    }

    @SneakyThrows
    public String getShotEffectsForItem(String itemId, Long userId, Localization localization) {
        Map<Long, UserShotEffect> userShotEffects = userShotEffectRepository
                .getUserShotEffectsForItem(userId, itemId).stream()
                .collect(Collectors.toMap(UserShotEffect::getShotEffectId, userShotEffect -> userShotEffect));

        List<ShotEffectItem> itemShotEffects = shotEffectItemRepository.getShotEffectItems(itemId);

        List<ItemShotEffectObject> response = itemShotEffects.stream()
                .map(shotEffectItem -> ItemShotEffectObject.builder()
                        .item(shotEffectItem.getItemId())
                        .title(localization == Localization.RU ? shotEffectItem.getTitleRu()
                                : shotEffectItem.getTitleEn())
                        .desc(localization == Localization.RU ? shotEffectItem.getDescRu() : shotEffectItem.getDescEn())
                        .price(shotEffectItem.getPrice())
                        .shotEffect(shotEffectItem.getClientId())
                        .bought(userShotEffects.containsKey(shotEffectItem.getId()))
                        .equipped(userShotEffects.containsKey(shotEffectItem.getId())
                                && userShotEffects.get(shotEffectItem.getId()).isMounted())
                        .build())
                .collect(Collectors.toList());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(response);
    }

    public void equipShotEffect(Long userId, String shotEffectClientId, boolean needCallback) {

        if (userShotEffectRepository.userHaveShotEffect(userId, shotEffectClientId)) {
            Optional<ShotEffectItem> shotEffectItem = shotEffectItemRepository
                    .findShotEffectByClientId(shotEffectClientId);
            userShotEffectRepository.unmountAllShotEffectsByItem(userId, shotEffectItem.get().getItemId());
            userShotEffectRepository.mountShotEffect(userId, shotEffectItem.get().getId());
            Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                    .ifPresent(lobbyManager -> {
                        lobbyManager.send(Type.GARAGE, "update_shot_effects_for_item",
                                getShotEffectsForItem(shotEffectItem.get().getItemId(),
                                        userId,
                                        lobbyManager.getLocalUser().getLocalization()));
                        if (needCallback) {
                            lobbyManager.send(Type.GARAGE, "mount_shot_effect", shotEffectItem.get().getItemId(),
                                    shotEffectClientId);
                        }
                    });
        }
    }

    public Optional<String> getMountedShotEffectForUserAndItem(String itemId, long userId) {
        return userShotEffectRepository.getMountedUserShotEffect(userId, itemId)
                .map(ShotEffectItem::getClientId);

    }

    public boolean existShotEffect(String clientId) {
        return shotEffectItemRepository.findShotEffectByClientId(clientId).isPresent();
    }
}
