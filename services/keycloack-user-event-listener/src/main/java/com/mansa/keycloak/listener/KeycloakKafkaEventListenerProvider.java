package com.mansa.keycloak.listener;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

public class KeycloakKafkaEventListenerProvider implements EventListenerProvider {

    private final KafkaProducer<String, String> producer;

    public KeycloakKafkaEventListenerProvider() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
    }

    // ================= USER EVENTS =================
    @Override
    public void onEvent(Event event) {

        if (EventType.REGISTER.equals(event.getType())) {

            String payload = buildUserCreatedEvent(event);

            producer.send(new ProducerRecord<>(
                    "auth.user.created.v1",
                    event.getUserId(),
                    payload
            ));
        }
    }

    // ================= ADMIN EVENTS (REQUIRED) =================
    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // pas utilisé pour le moment
    }

    @Override
    public void close() {
        producer.close();
    }

    private String buildUserCreatedEvent(Event event) {
        return "{"
                + "\"eventId\":\"" + UUID.randomUUID() + "\","
                + "\"eventType\":\"USER_CREATED\","
                + "\"eventVersion\":1,"
                + "\"occurredAt\":\"" + Instant.now() + "\","
                + "\"userId\":\"" + event.getUserId() + "\","
                + "\"username\":\"" + event.getDetails().get("username") + "\","
                + "\"email\":\"" + event.getDetails().get("email") + "\""
                + "}";
    }
}