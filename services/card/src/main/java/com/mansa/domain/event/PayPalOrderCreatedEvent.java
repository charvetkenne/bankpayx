package com.mansa.domain.event;

public class PayPalOrderCreatedEvent extends DomainEvent {

    private final String transactionId;
    private final String paypalOrderId;

    public PayPalOrderCreatedEvent(String transactionId, String paypalOrderId, String correlationId) {
        super(correlationId);
        this.transactionId = transactionId;
        this.paypalOrderId = paypalOrderId;
    }

    public String getTransactionId() { return transactionId; }
    public String getPaypalOrderId() { return paypalOrderId; }
}
