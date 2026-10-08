package com.mansa.domain.event;

public class PaymentFailedEvent extends DomainEvent {

    private final String transactionId;
    private final String reason;

    public PaymentFailedEvent(String transactionId, String reason, String correlationId) {
        super(correlationId);
        this.transactionId = transactionId;
        this.reason        = reason;
    }

    public String getTransactionId() { return transactionId; }
    public String getReason()        { return reason; }
}
