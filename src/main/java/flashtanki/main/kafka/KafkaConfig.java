package flashtanki.main.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaConfig {
    private static final String KAFKA_SERVER = "5.189.131.63:9092";
    private static final String SECURITY_PROTOCOL = "SASL_PLAINTEXT";
    private static final String SASL_MECHANISM = "PLAIN";
    private static final String SASL_JAAS = "org.apache.kafka.common.security.plain.PlainLoginModule required";
    private static final String KAFKA_USERNAME = "admin";
    private static final String KAFKA_PASSWORD = "fdsa432tFDSfdsf43f3";
    private static final String GROUP_ID = "flashtanki-group";

    private final Properties properties;

    public KafkaConfig() {
        properties = new Properties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL);
        properties.put(SaslConfigs.SASL_MECHANISM, SASL_MECHANISM);
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(
                "%s username=\"%s\" password=\"%s\";",
                SASL_JAAS, KAFKA_USERNAME, KAFKA_PASSWORD
        ));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    }

    public Properties getProperties() {
        return properties;
    }

    public void setAdditionalProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getPropertyValue(String key) {
        return properties.getProperty(key);
    }
}
