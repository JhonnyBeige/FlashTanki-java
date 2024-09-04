package flashtanki.containers;

import flashtanki.commands.Type;
import flashtanki.main.kafka.KafkaTemplateService;
import flashtanki.main.kafka.MessageConsumer;
import flashtanki.lobby.LobbyManager;
import flashtanki.services.LobbysServices;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class PopulateContainerWindowService implements MessageConsumer.ExternalMessageListener {
   private KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
   private static PopulateContainerWindowService instance;
   private final String CONTAINER_WINDOW_RESPONSE_TOPIC = "container-window-response";

   public static PopulateContainerWindowService getInstance() {
      if (instance == null) {
         instance = new PopulateContainerWindowService();
      }
      return instance;
   }

   private PopulateContainerWindowService() {
      kafkaTemplateService.getConsumer().addListener(CONTAINER_WINDOW_RESPONSE_TOPIC, this);
   }

   @Override
   @SneakyThrows
   public void onReceive(String message) {
      ObjectMapper objectMapper = new ObjectMapper();
      GenericContainerWindowResponse resp = objectMapper.readValue(message, GenericContainerWindowResponse.class);
      // Now the item object is populated with the data from the JSON string
      LobbyManager lobbyManager = LobbysServices.getInstance()
            .getLobbyByUserId(resp.userId);
      String outString = objectMapper.writeValueAsString(resp.items);
      if (lobbyManager != null) {
         lobbyManager.send(Type.LOBBY, "container_window", outString);
      }
   }

   @Data
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   private static class GenericContainerWindowResponse {
      private Long userId;
      private ArrayList<GenericContainerWindowResponseItem> items;
   }

   @Data
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   private static class GenericContainerWindowResponseItem {
      private String id;
      private String name;
      private String rarity;
      private int count;
   }
}