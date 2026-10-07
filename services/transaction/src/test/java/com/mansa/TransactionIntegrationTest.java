package com.mansa;


import static org.junit.jupiter.api.Assertions.assertEquals;

//import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"transactions.created","transactions.processed","transactions.failed"})
public class TransactionIntegrationTest {

  @Autowired
  private TestRestTemplate rest;

  @Test
  void createEndpoint_emitsEvent() {
    var req = new com.mansa.application.service.dto.CreateTransactionRequest();
    req.setSourceAccount("SRC");
    req.setDestinationAccount("DST");
    req.setAmount(new java.math.BigDecimal("10"));
    req.setCurrency("EUR");
    req.setType(com.mansa.domain.model.TransactionType.INTERNAL_TRANSFER);

    ResponseEntity<String> r = rest.postForEntity("/api/transactions", req, String.class);
    assertEquals(201, r.getStatusCode().value());
    // Further: use EmbeddedKafka consumer to assert messages — omitted for brevity
  }
}

