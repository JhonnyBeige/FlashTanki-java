package flashtanki.main.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.*;

public class MessageConsumer {
    private KafkaConfig kafkaConfig;

    private Map<String, List<ExternalMessageListener>> messageListeners;

    public MessageConsumer(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
        this.messageListeners = new HashMap<>();
        new Thread(this::consume).start();
    }

    public void consume() {
        Consumer<String, String> consumer = new KafkaConsumer<>(kafkaConfig.getProperties());
        consumer.subscribe(List.of("get-challenge-info-response", "give-item-request", "update-stars-request",
                "container-window-response", "container-open-response"));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));

                records.forEach(record -> {
                    Optional.ofNullable(messageListeners.get(record.topic()))
                            .ifPresent(topicListeners -> topicListeners.forEach(listener -> {
                                try {
                                    listener.onReceive(record.value());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }));
                });
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    public void addListener(String topic, ExternalMessageListener messageListener) {
        List<ExternalMessageListener> externalMessageListeners = messageListeners.computeIfAbsent(topic,
                k -> new ArrayList<>());
        externalMessageListeners.add(messageListener);
    }

    public interface ExternalMessageListener {
        void onReceive(String message);
    }
}
