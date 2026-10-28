package com.mansa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${app.messaging.enabled:true}")
    private boolean messagingEnabled;

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(),
                new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(3);
        // Point de contrôle réel de l'auto-startup : spring.kafka.listener.auto-startup
        // est ignoré pour ce bean car il est déclaré manuellement (hors autoconfig Boot).
        factory.setAutoStartup(messagingEnabled);
        return factory;
    }
}






























































// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.apache.kafka.clients.consumer.ConsumerConfig;
// import org.apache.kafka.clients.producer.ProducerConfig;
// import org.apache.kafka.common.serialization.StringDeserializer;
// import org.apache.kafka.common.serialization.StringSerializer;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
// import org.springframework.kafka.core.*;
// import org.springframework.kafka.listener.ContainerProperties;
// import org.springframework.kafka.support.serializer.JsonSerializer;

// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// public class KafkaConfig {

//     @Value("${spring.kafka.bootstrap-servers}")
//     private String bootstrapServers;

//     @Value("${spring.kafka.consumer.group-id}")
//     private String groupId;

//     @Bean
//     public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
//         Map<String, Object> config = new HashMap<>();
//         config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//         config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//         config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//         config.put(ProducerConfig.ACKS_CONFIG, "all");
//         config.put(ProducerConfig.RETRIES_CONFIG, 3);
//         config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
//         config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
//         config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
//         return new DefaultKafkaProducerFactory<>(config, new StringSerializer(),
//                 new JsonSerializer<>(objectMapper));
//     }

//     @Bean
//     public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
//         return new KafkaTemplate<>(producerFactory);
//     }

//     @Bean
//     public ConsumerFactory<String, String> consumerFactory() {
//         Map<String, Object> config = new HashMap<>();
//         config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//         config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//         config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//         config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//         config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//         config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//         config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
//         return new DefaultKafkaConsumerFactory<>(config);
//     }

//     @Bean
//     public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
//             ConsumerFactory<String, String> consumerFactory) {
//         ConcurrentKafkaListenerContainerFactory<String, String> factory =
//                 new ConcurrentKafkaListenerContainerFactory<>();
//         factory.setConsumerFactory(consumerFactory);
//         factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
//         factory.setConcurrency(3);
//         return factory;
//     }
// }
