package gtanks.containers;

import com.fasterxml.jackson.databind.ObjectMapper;

import gtanks.commands.Type;
import gtanks.containers.list.ContainerItemInfo;
import gtanks.containers.list.ContainerAssortmentRespository;
import gtanks.containers.users.UserContainer;
import gtanks.containers.users.UserContainerRepository;
import gtanks.kafka.KafkaTemplateService;
import gtanks.services.LobbysServices;
import gtanks.shoteffect.ShotEffectSystem;
import gtanks.shoteffect.list.ShotEffectItem;
import gtanks.shoteffect.user.UserShotEffect;
import gtanks.skin.SkinSystem;
import gtanks.skin.list.SkinItem;
import gtanks.skin.user.UserSkin;
import gtanks.system.localization.Localization;
import gtanks.users.User;
import gtanks.users.garage.items.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerSystem {
    private static ContainerSystem instance;
    private final ContainerAssortmentRespository containerAssortmentRespository = ContainerAssortmentRespository
            .getInstance();
    private final UserContainerRepository userContainerRepository = UserContainerRepository.getInstance();
    private final LobbysServices lobbysServices = LobbysServices.getInstance();
    private final String CONTAINER_WINDOW_REQUEST_TOPIC = "container-window-request";
    private final String CONTAINER_OPEN_REQUEST_TOPIC = "container-open-request";
    private final KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
    private static SkinSystem skinSystem = SkinSystem.getInstance();
    private static ShotEffectSystem shotEffectSystem = ShotEffectSystem.getInstance();

    public static ContainerSystem getInstance() {
        if (ContainerSystem.instance == null) {
            ContainerSystem.instance = new ContainerSystem();
        }
        return ContainerSystem.instance;
    }

    private ContainerSystem() {
    }

    @SneakyThrows
    public String getUserContainersResponse(Long userId) {
        Map<Long, ContainerItemInfo> containers = containerAssortmentRespository
                .findAll()
                .stream()
                .collect(Collectors.toMap(ContainerItemInfo::getId, container -> container));

        List<ListContainerResponse.ContainerInfo> containersInfo = userContainerRepository
                .getUserContainers(userId)
                .stream()
                .map(container -> {
                    Localization localization = lobbysServices
                            .getLobbyByUserId(userId)
                            .getLocalUser()
                            .getLocalization();
                    ListContainerResponse.ContainerInfo info = new ListContainerResponse.ContainerInfo();
                    info.setId(containers.get(container.getContainerId()).getClientId());
                    info.setTitle(
                            localization == Localization.EN ? containers.get(container.getContainerId()).getTitleEn()
                                    : containers.get(container.getContainerId()).getTitleRu());
                    info.setDesc(
                            localization == Localization.EN ? containers.get(container.getContainerId()).getDescEn()
                                    : containers.get(container.getContainerId()).getDescRu());
                    info.setCount(container.getCount());
                    return info;
                })
                .collect(Collectors.toList());

        ListContainerResponse response = new ListContainerResponse();
        response.setList(containersInfo);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(response);
    }

    public void giveContainer(Long userId, String containerClientId, int count) {
        containerAssortmentRespository.findByClientId(containerClientId)
                .ifPresent(containerItemInfo -> {
                    if (userContainerRepository.existUserContainers(userId, containerItemInfo.getId()) ) {
                        userContainerRepository.addContainers(userId, containerItemInfo.getId(), count);
                    } else {
                        userContainerRepository.save(UserContainer.builder()
                                .userId(userId)
                                .containerId(containerItemInfo.getId())
                                .count(count)
                                .build());
                    }
                });
    }

    @SneakyThrows
    public void openContainer(User user, String clientContainerId) {
        Long userId = user.getId();
        Map<String, Long> containers = containerAssortmentRespository.findAll()
                .stream()
                .collect(Collectors.toMap(ContainerItemInfo::getClientId, ContainerItemInfo::getId));

        int countContainers = userContainerRepository.countUserContainers(userId, containers.get(clientContainerId));
        if (countContainers > 0) {
            userContainerRepository.decrementContainer(userId, containers.get(clientContainerId));
            String userContainers = getUserContainersResponse(userId);

            lobbysServices.getLobbyByUserId(userId).send(Type.GARAGE,
                    "init_containers",
                    userContainers);

            // ObjectMapper objectMapper = new ObjectMapper();
            // List<String> outItems = new ArrayList<String>();

            // for (Item item : user.getGarage().items) {
            //     outItems.add(item.getId());
            // }
            // Map<Long, ShotEffectItem> allShotEffects = shotEffectSystem.getAllShotEffects();
            // List<UserShotEffect> userShotEffects = shotEffectSystem.getAllUserShotEffects(user.getId());
            // Map<Long, SkinItem> allSkins = skinSystem.getAllSkins();
            // List<UserSkin> userSkins = skinSystem.getAllUserSkins(user.getId());

            // for (UserShotEffect userShotEffect : userShotEffects) {
            //     outItems.add(allShotEffects.get(userShotEffect.getShotEffectId()).getClientId());
            // }
            // for (UserSkin userSkin : userSkins) {
            //     outItems.add(allSkins.get(userSkin.getSkinId()).getClientId());
            // }

            // ContainerWindowRequest containerWindowRequest = ContainerWindowRequest.builder()
            //         .userId(user.getId())
            //         .containerId(clientContainerId)
            //         .items(outItems)
            //         .build();

            // String outStr = objectMapper.writeValueAsString(containerWindowRequest);
            //FIXME: no kafka
            //kafkaTemplateService.getProducer().send(outStr, CONTAINER_OPEN_REQUEST_TOPIC);
        }
    }

    @SneakyThrows
    public void openContainerWindow(User user, String clientContainerId) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> outItems = new ArrayList<String>();

        for (Item item : user.getGarage().items) {
            outItems.add(item.getId());
        }
        Map<Long, ShotEffectItem> allShotEffects = shotEffectSystem.getAllShotEffects();
        List<UserShotEffect> userShotEffects = shotEffectSystem.getAllUserShotEffects(user.getId());
        Map<Long, SkinItem> allSkins = skinSystem.getAllSkins();
        List<UserSkin> userSkins = skinSystem.getAllUserSkins(user.getId());

        for (UserShotEffect userShotEffect : userShotEffects) {
            outItems.add(allShotEffects.get(userShotEffect.getShotEffectId()).getClientId());
        }
        for (UserSkin userSkin : userSkins) {
            outItems.add(allSkins.get(userSkin.getSkinId()).getClientId());
        }

        ContainerWindowRequest containerWindowRequest = ContainerWindowRequest.builder()
                .userId(user.getId())
                .containerId(clientContainerId)
                .items(outItems)
                .build();

        String outStr = objectMapper.writeValueAsString(containerWindowRequest);
        //FIXME: no kafka
        //kafkaTemplateService.getProducer().send(outStr, CONTAINER_WINDOW_REQUEST_TOPIC);
    }

    public boolean existContainer(String itemId) {
        return containerAssortmentRespository.findByClientId(itemId).isPresent();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ContainerWindowRequest {
        private Long userId;
        private String containerId;
        private List<String> items;

    }
}
