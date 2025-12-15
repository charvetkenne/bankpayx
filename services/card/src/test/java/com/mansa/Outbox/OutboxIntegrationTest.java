package com.mansa.Outbox;


import com.mansa.dto.AuthorizationRequestDTO;
import com.mansa.dto.AuthorizationResponseDTO;
import com.mansa.outbox.OutboxEvent;
import com.mansa.outbox.OutboxEventRepository;
import com.mansa.outbox.OutboxPublisher;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OutboxIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("carddb")
            .withUsername("card")
            .withPassword("cardpwd");

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.4.0");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", postgres::getJdbcUrl);
        reg.add("spring.datasource.username", postgres::getUsername);
        reg.add("spring.datasource.password", postgres::getPassword);
        reg.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        // disable scheduled publisher to make test deterministic (we will call flush manually)
        reg.add("outbox.publisher.delay", () -> "60000");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Test
    void authorize_endpoint_should_create_outbox_and_publish_to_kafka() throws Exception {
        // 1) call the API
        AuthorizationRequestDTO req = new AuthorizationRequestDTO();
        req.pan = "4111111111111111";
        req.expiry = "2512";
        req.amount = new BigDecimal("10.00");
        req.currency = "EUR";
        req.merchantId = "M-INT-01";
        req.rrn = "RRN-IT-001"; 

        var response = restTemplate.postForEntity("/api/cards/authorize", req, AuthorizationResponseDTO.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        AuthorizationResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status).isIn("AUTHORIZED", "DECLINED");

        // 2) assert outbox row created
        List<OutboxEvent> events = outboxRepository.findAll();
        assertThat(events).isNotEmpty();
        OutboxEvent evt = events.get(0);
        assertThat(evt.getStatus()).isEqualTo(com.mansa.outbox.OutboxEventStatus.PENDING);

        // 3) flush pending events (calls kafkaTemplate synchronously inside)
        outboxPublisher.flushPendingBatch(10);

        // 4) create a consumer and verify message arrived
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "it-test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String,String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList("card.authorizations"));
            ConsumerRecords<String,String> records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.count()).isGreaterThan(0);

            boolean found = StreamSupport.stream(records.records("card.authorizations").spliterator(), false)
                    .anyMatch(r -> r.value() != null && r.value().contains("\"rrn\""));
            assertThat(found).isTrue();
        }
    }
}

