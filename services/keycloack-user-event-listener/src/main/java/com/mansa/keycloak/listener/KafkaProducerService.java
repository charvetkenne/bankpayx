package com.mansa.keycloak.listener;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansa.keycloak.listener.model.UserCreatedEvent;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

public class KafkaProducerService {

    private final Producer<String, String> producer;
    private final ObjectMapper mapper = new ObjectMapper();

    public KafkaProducerService() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
    }

    public void send(UserCreatedEvent event) {
        try {
            String json = mapper.writeValueAsString(event);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>("auth.user.created.v1", event.eventType(), json);

            producer.send(record);
        } catch (Exception e) {
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}