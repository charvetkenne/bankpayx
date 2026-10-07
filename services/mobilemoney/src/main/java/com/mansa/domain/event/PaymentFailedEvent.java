package com.mansa.domain.event;



import com.mansa.domain.valueobject.*;

public final class PaymentFailedEvent extends DomainEvent {

    public static final String EVENT_TYPE = "PAYMENT_FAILED";

    private final String transactionId;
    private final String operatorCode;
    private final String failureReason;
    private final String failureCode;

    public PaymentFailedEvent(
            String correlationId,
            TransactionId transactionId,
            OperatorCode operatorCode,
            String failureReason,
            String failureCode) {
        super(correlationId);
        this.transactionId = transactionId.toString();
        this.operatorCode = operatorCode.name();
        this.failureReason = failureReason;
        this.failureCode = failureCode;
    }

    @Override
    public String getEventType() { return EVENT_TYPE; }

    @Override
    public String getAggregateId() { return transactionId; }

    public String getTransactionId() { return transactionId; }
    public String getOperatorCode() { return operatorCode; }
    public String getFailureReason() { return failureReason; }
    public String getFailureCode() { return failureCode; }
}
