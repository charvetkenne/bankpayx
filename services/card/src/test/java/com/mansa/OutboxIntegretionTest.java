package com.mansa;

// import java.math.BigDecimal;
// import java.time.Duration;
// import java.util.List;
// import java.util.Properties;
// import java.util.stream.Collectors;
// import java.util.stream.StreamSupport;

// import org.apache.kafka.clients.consumer.ConsumerConfig;
// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.apache.kafka.clients.consumer.ConsumerRecords;
// import org.apache.kafka.clients.consumer.KafkaConsumer;
// import org.apache.kafka.common.serialization.StringDeserializer;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.kafka.test.utils.KafkaTestUtils;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.KafkaContainer;
// import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

// import org.testcontainers.junit.jupiter.Container;
// import static org.assertj.core.api.Assertions.assertThat;

// import java.util.stream.StreamSupport;


// import com.mansa.dto.AuthorizationRequestDTO;
// import com.mansa.dto.AuthorizationResponseDTO;
// import com.mansa.outbox.OutboxPublisher;

// import scala.collection.Iterable;



//import net.bytebuddy.utility.dispatcher.JavaDispatcher.Container;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 class OutboxIntegrationTest {
//   @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
//      .withDatabaseName("carddb").withUsername("card").withPassword("cardpwd");
//   @Container static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.4.0");

//   @DynamicPropertySource
//   static void props(DynamicPropertyRegistry reg){
//     reg.add("spring.datasource.url", postgres::getJdbcUrl);
//     reg.add("spring.datasource.username", postgres::getUsername);
//     reg.add("spring.datasource.password", postgres::getPassword);
//     reg.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//     reg.add("outbox.publisher.delay", () -> "10000"); // disable scheduled quick runs
//   }

//   @Autowired TestRestTemplate rest;
//   @Autowired OutboxPublisher publisher;

//   @Test
//   void authorize_writes_outbox_and_publishes_event() throws Exception {
//     // call authorize endpoint
//     AuthorizationRequestDTO req = new AuthorizationRequestDTO();
//     req.pan="4111111111111111"; req.expiry="2512"; req.amount=new BigDecimal("10"); req.currency="EUR";
//     var resp = rest.postForEntity("/api/cards/authorize", req, AuthorizationResponseDTO.class);
//     assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();

//     // run publisher manually (flush small batch)
//     publisher.flushPendingBatch(10);

//     // consume topic and assert record present
//     Properties consumerProps = new Properties();
//     consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
//     consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
//     consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//     consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//     consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

//     try (KafkaConsumer<String,String> consumer = new KafkaConsumer<>(consumerProps)) {
//       consumer.subscribe(List.of("card.authorizations"));

//       ConsumerRecords<String,String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
//       assertThat(records.count()).isGreaterThan(0);
//       boolean found = records.records("card.authorizations")
//           .StreamSupport.stream().anyMatch(r -> r.value().contains("\"rrn\""));
//       assertThat(found).isTrue();
//     }
//   }
//    @Test
//    void shouldConsumeOutboxEvents() {

//         Object kafkaTestUtils;
//         Iterable<ConsumerRecord<String, String>> records = kafkaTestUtils.getConsumedRecords();

//         List<ConsumerRecord<String, String>> list =
//                 StreamSupport.stream(records.iterator(), false)
//                              .collect(Collectors.toList());

//         assertThat(list).isNotEmpty();
//         assertThat(list.get(0).value()).isNotBlank();
//     }

}

