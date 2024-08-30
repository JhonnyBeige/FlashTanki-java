package gtanks.premium;

import com.fasterxml.jackson.databind.ObjectMapper;
import gtanks.commands.Type;
import gtanks.kafka.KafkaTemplateService;
import gtanks.kafka.MessageConsumer;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerImpl;
import gtanks.services.LobbysServices;
import gtanks.services.hibernate.HibernateService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PremiumService implements MessageConsumer.ExternalMessageListener {
    private static PremiumService instance;
    private final Map<Long, Premium> cache = new HashMap<>();

    public static PremiumService getInstance() {
        if (instance == null) {
            instance = new PremiumService();
        }
        return instance;
    }

    private PremiumService() {
    }

    public void activatePremium(Long userId, Long time) {
        boolean activated = false;
        Premium premium = getPremium(userId);
        if (premium.getTime().isAfter(LocalDateTime.now())) {
            premium.setTime(premium.getTime().plusSeconds(time));
        } else {
            premium.setTime(LocalDateTime.now().plusSeconds(time));
            activated = true;
        }
        Session session = HibernateService.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        session.update(premium);
        transaction.commit();
        cache.put(userId, premium);
        if (activated) {
            Optional.ofNullable(LobbysServices.getInstance().getLobbyByUserId(userId))
                    .ifPresent(lobbyManager -> lobbyManager.send(Type.LOBBY, "enable_premium"));
        }
    }

    public PremiumDto getPremiumTime(Long userId) {
        Premium premium = cache.get(userId);
        if (premium == null) {
            premium = getPremium(userId);
            cache.put(userId, premium);
        }
        return PremiumDto.builder()
                .userId(premium.getUserId())
                .time(premium.getTime())
                .isActivated(premium.getTime().isAfter(LocalDateTime.now()))
                .build();
    }

    private Premium getPremium(Long userId) {
        Session session = HibernateService.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        Premium premium1 = Optional.ofNullable(session.get(Premium.class, userId))
                .orElseGet(() -> {
                    Premium premium = Premium.builder()
                            .userId(userId)
                            .time(LocalDateTime.now())
                            .build();
                    session.save(premium);
                    return premium;
                });

        transaction.commit();
        return premium1;
    }

    @Override
    public void onReceive(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ActivatePremiumRequest request = objectMapper.readValue(message, ActivatePremiumRequest.class);
            activatePremium(request.getUserId(), request.getSeconds());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivatePremiumRequest {
        public Long userId;
        public Long seconds;
    }
}
