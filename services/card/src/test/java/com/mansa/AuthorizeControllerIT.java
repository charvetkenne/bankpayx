package com.mansa;

import com.mansa.outbox.OutboxPublisher;
import com.mansa.outbox.OutboxEventRepository;
import com.mansa.outbox.OutboxEvent;
import com.mansa.dto.AuthorizationRequestDTO;
import com.mansa.dto.AuthorizationResponseDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
 
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthorizeControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("carddb")
            .withUsername("card")
            .withPassword("cardpwd");

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.4.0");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        // speed up scheduled publisher in tests (or we'll call flush manually)
        registry.add("outbox.publisher.delay", () -> "60000"); // effectively disabled
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Test
    void endToEnd_authorize_createsOutboxAndPublishesToKafka() throws Exception {
        // 1) call the authorize endpoint
        AuthorizationRequestDTO req = new AuthorizationRequestDTO();
        req.pan = "4111111111111111";
        req.expiry = "2512";
        req.amount = new BigDecimal("12.34");
        req.currency = "EUR";
        req.merchantId = "M123";
        req.rrn = "RRN-IT-001";

        var respEntity = restTemplate.postForEntity("/api/cards/authorize", req, AuthorizationResponseDTO.class);
        assertThat(respEntity.getStatusCode().is2xxSuccessful()).isTrue();
        AuthorizationResponseDTO resp = respEntity.getBody();
        assertThat(resp).isNotNull();
        assertThat(resp.status).isIn("AUTHORIZED","DECLINED");

        // 2) ensure OutboxEvent exists in DB
        List<OutboxEvent> all = outboxRepository.findAll();
        assertThat(all).isNotEmpty();
        OutboxEvent outbox = all.get(0);
        assertThat(outbox.getStatus()).isEqualTo(outbox.getStatus()); // trivial check that record exists

        // 3) flush pending events synchronously
        outboxPublisher.flushPendingBatch(10);

        // 4) consume Kafka topic to assert message arrived
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

            boolean found = false;
            for (ConsumerRecord<String,String> r : records) {
                if (r.value() != null && r.value().contains("\"rrn\"")) {
                    found = true;
                    break;
                }
            }
            assertThat(found).isTrue();
        }
    }
}
