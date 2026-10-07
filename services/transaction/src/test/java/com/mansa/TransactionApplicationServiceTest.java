package com.mansa;



import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.TransactionRepositoryPort;
import com.mansa.application.service.TransactionApplicationService;
import com.mansa.application.service.dto.CreateTransactionRequest;
import com.mansa.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionApplicationServiceTest {

  @Mock TransactionRepositoryPort repository;
  @Mock EventPublisherPort publisher;

  TransactionApplicationService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new TransactionApplicationService(repository, publisher);
  }

  @Test
  void create_validRequest_savesAndPublishes() {
    CreateTransactionRequest req = new CreateTransactionRequest();
    req.setSourceAccount("ACC1");
    req.setDestinationAccount("ACC2");
    req.setAmount(new BigDecimal("100.00"));
    req.setCurrency("EUR");
    req.setType(TransactionType.CARD_PAYMENT);

    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    var resp = service.create(req);

    assertNotNull(resp);
    assertEquals("EUR", resp.getCurrency());
    verify(repository, times(1)).save(any());
    verify(publisher, times(1)).publish(eq("transactions.created"), any());
  }
}
