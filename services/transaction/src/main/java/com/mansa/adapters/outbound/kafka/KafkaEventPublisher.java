package com.mansa.adapters.outbound.kafka;


import com.mansa.application.port.out.EventPublisherPort;

import lombok.Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Data
@Component
public class KafkaEventPublisher implements EventPublisherPort {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper mapper;

  public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper mapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.mapper = mapper;
  }

  @SuppressWarnings("null")
  @Override
  public void publish(String topic, Object event) {
    // key peut être l'UUID si l'événement l'expose; ici simple
    kafkaTemplate.send(topic, event);
  }
}
