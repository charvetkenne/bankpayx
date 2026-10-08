package com.mansa.outbox;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    private UUID id;

    private String aggregateType;
    private String aggregateId;
    private String topic;
    private String partitionKey;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status;

    private int attemptCount;
    private Instant lastAttempt;
    private Instant createdAt;

    protected OutboxEvent() {}

    public OutboxEvent(
            String aggregateType,
            String aggregateId,
            String topic,
            String partitionKey,
            String payload
    ) {
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.partitionKey = partitionKey;
        this.payload = payload;
        this.status = OutboxEventStatus.PENDING;
        this.attemptCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getTopic() { return topic; }
    public String getPartitionKey() { return partitionKey; }
    public String getPayload() { return payload; }
    public OutboxEventStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getLastAttempt() { return lastAttempt; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(OutboxEventStatus status) { this.status = status; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public void setLastAttempt(Instant lastAttempt) { this.lastAttempt = lastAttempt; }
}
