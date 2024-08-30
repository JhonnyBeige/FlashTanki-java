package gtanks.kafka;

import gtanks.logger.LogType;
import gtanks.logger.LoggerService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class MessageProducer {
    private KafkaConfig kafkaConfig;
    private final static LoggerService loggerService = LoggerService.getInstance();

    public MessageProducer(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    public void send(String message, String topic) {
        Producer<String, String> producer = new KafkaProducer<>(kafkaConfig.getProperties());

        try {
            producer.send(new ProducerRecord<>(topic, message),
                    (metadata, exception) -> {
                        if (exception == null) {
                            loggerService.log(LogType.INFO,"Message sent successfully! Topic: " + metadata.topic() +
                                    ", Partition: " + metadata.partition() +
                                    ", Offset: " + metadata.offset());
                        } else {
                            exception.printStackTrace();
                        }
                    });
        } finally {
            producer.close();
        }
    }
}
