package flashtanki.users.garage.containers;

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

public class OpenContainerService implements MessageConsumer.ExternalMessageListener {
   private KafkaTemplateService kafkaTemplateService = KafkaTemplateService.getInstance();
   private static OpenContainerService instance;
   private final String CONTAINER_OPEN_RESPONSE_TOPIC = "container-open-response";

   public static OpenContainerService getInstance() {
      if (instance == null) {
         instance = new OpenContainerService();
      }
      return instance;
   }

   private OpenContainerService() {
      kafkaTemplateService.getConsumer().addListener(CONTAINER_OPEN_RESPONSE_TOPIC, this);
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
         lobbyManager.send(Type.LOBBY, "container_opened", outString);
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