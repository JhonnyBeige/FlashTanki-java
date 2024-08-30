package gtanks.kafka;

public class KafkaTemplateService {
    private static KafkaTemplateService instance;
    private MessageProducer producer;
    private MessageConsumer consumer;

    private KafkaTemplateService(MessageProducer producer, MessageConsumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public static synchronized KafkaTemplateService getInstance() {
        if (instance == null) {
            KafkaConfig kafkaConfig = new KafkaConfig();
            MessageProducer messageProducer = new MessageProducer(kafkaConfig);
            MessageConsumer messageConsumer = new MessageConsumer(kafkaConfig);
            instance = new KafkaTemplateService(messageProducer, messageConsumer);
        }
        return instance;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public MessageConsumer getConsumer() {
        return consumer;
    }
}