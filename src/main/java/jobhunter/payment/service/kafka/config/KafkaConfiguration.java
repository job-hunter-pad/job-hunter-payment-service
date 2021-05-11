package jobhunter.payment.service.kafka.config;


import jobhunter.payment.service.models.customer.UserProfile;
import jobhunter.payment.service.models.payment.JobOfferPayment;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfiguration {


    @Bean
    public ConsumerFactory<String, UserProfile> userProfileConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_profile_payment");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        JsonDeserializer<UserProfile> jobApplicationJsonDeserializer = new JsonDeserializer<>(UserProfile.class, false);
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), jobApplicationJsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserProfile> userProfileKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserProfile> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userProfileConsumerFactory());
        return factory;
    }


    @Bean
    public ProducerFactory<String, JobOfferPayment> jobOfferPaymentProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, JobOfferPayment> jobOfferPaymentKafkaTemplate() {
        return new KafkaTemplate<>(jobOfferPaymentProducerFactory());
    }
}