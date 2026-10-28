package com.mansa.infrastructure.kafka.consumer;


import com.mansa.application.port.in.CancelTransactionPort;
import com.mansa.application.port.in.RetryTransactionPort;
import com.mansa.application.usecase.CancelTransactionUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionServiceEventConsumer {

    private final CancelTransactionPort cancelTransactionPort;
    private final RetryTransactionPort retryTransactionPort;
    private final ObjectMapper objectMapper;

    /**
     * Consumes events from the transaction-service.
     * Implements Dead Letter Queue via @RetryableTopic with exponential backoff.
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = "-dlt",
            autoCreateTopics = "false"
    )
    @KafkaListener(
            topics = "${kafka.topics.transaction-commands}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionCommand(ConsumerRecord<String, String> record) {
        String correlationId = record.key();
        log.info("Received transaction command: key={}, offset={}", correlationId, record.offset());

        try {
            JsonNode payload = objectMapper.readTree(record.value());
            String commandType = payload.path("commandType").asText();
            String transactionId = payload.path("transactionId").asText();

            switch (commandType) {
                case "CANCEL_PAYMENT" -> {
                    String reason = payload.path("reason").asText("Cancelled by transaction-service");
                    String requestedBy = payload.path("requestedBy").asText("transaction-service");
                    cancelTransactionPort.cancelTransaction(
                            new CancelTransactionUseCase.CancelCommand(transactionId, reason, requestedBy)
                    );
                    log.info("Payment cancelled by transaction-service command: txId={}", transactionId);
                }
                case "RETRY_PAYMENT" -> {
                    retryTransactionPort.retryTransaction(transactionId);
                    log.info("Payment retry triggered by transaction-service: txId={}", transactionId);
                }
                default -> log.warn("Unknown command type '{}', skipping: key={}", commandType, correlationId);
            }

        } catch (Exception e) {
            log.error("Error processing transaction command: key={}, error={}", correlationId, e.getMessage());
            throw new RuntimeException("Failed to process transaction command", e);
        }
    }
}
