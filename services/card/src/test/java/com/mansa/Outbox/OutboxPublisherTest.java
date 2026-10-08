package com.mansa.Outbox;


import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.mansa.outbox.OutboxEvent;
import com.mansa.outbox.OutboxEventRepository;
import com.mansa.outbox.OutboxPublisher;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publisher = new OutboxPublisher(repository, kafkaTemplate);
    }

    // @Test
    // void flushPendingBatch_sendsPendingEvents() throws Exception {
    //     OutboxEvent e = new OutboxEvent("CardTransaction", UUID.randomUUID().toString(), "card.authorizations", "key-1", "{\"rrn\":\"R1\"}");
    //     List<OutboxEvent> list = List.of(e);

    //     when(repository.findAndLockNextPending(PageRequest.of(0, 20))).thenReturn(list);
    //     when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(org.apache.kafka.clients.producer.RecordMetadata::toString);

    //     // call flush (it will call repository.save internally)
    //     publisher.flushPendingBatch(20);

    //     verify(repository, atLeastOnce()).findAndLockNextPending(PageRequest.of(0, 20));
    //     // after successful send the publisher will save event status; ensure repo.save called at least once
    //     verify(repository, atLeastOnce()).save(any(OutboxEvent.class));
    // }
    @Test
    void flushPendingBatch_sendsPendingEvents() throws Exception {

        OutboxEvent e = new OutboxEvent(
                "CardTransaction",
                UUID.randomUUID().toString(),
                "card.authorizations",
                "key-1",
                "{\"rrn\":\"R1\"}"
        );

        List<OutboxEvent> list = List.of(e);

        // mock repository
        when(repository.findAndLockNextPending(PageRequest.of(0, 20))).thenReturn(list);

        // create fake kafka SendResult
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("card.authorizations", 0),
                0, 0, System.currentTimeMillis(),
                0L, 0, 0
        );

        SendResult<String,String> sendResult = new SendResult<>(null, metadata);
        CompletableFuture<SendResult<String,String>> future = CompletableFuture.completedFuture(sendResult);

        // mock kafkaTemplate.send(...)
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // execute
        publisher.flushPendingBatch(20);

        // verify interactions
        verify(repository, atLeastOnce()).findAndLockNextPending(PageRequest.of(0, 20));
        verify(repository, atLeastOnce()).save(any(OutboxEvent.class));
    }

}
