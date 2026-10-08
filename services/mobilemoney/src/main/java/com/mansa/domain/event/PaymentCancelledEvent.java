package com.mansa.domain.event;


import com.mansa.domain.valueobject.*;

public final class PaymentCancelledEvent extends DomainEvent {

    public static final String EVENT_TYPE = "PAYMENT_CANCELLED";

    private final String transactionId;
    private final String operatorCode;
    private final String cancellationReason;

    public PaymentCancelledEvent(
            String correlationId,
            TransactionId transactionId,
            OperatorCode operatorCode,
            String cancellationReason) {
        super(correlationId);
        this.transactionId = transactionId.toString();
        this.operatorCode = operatorCode.name();
        this.cancellationReason = cancellationReason;
    }

    @Override
    public String getEventType() { return EVENT_TYPE; }

    @Override
    public String getAggregateId() { return transactionId; }

    public String getTransactionId() { return transactionId; }
    public String getOperatorCode() { return operatorCode; }
    public String getCancellationReason() { return cancellationReason; }
}