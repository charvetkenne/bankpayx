package com.mansa.domain.event;

import java.math.BigDecimal;

import com.mansa.domain.valueobject.*;

public final class PaymentInitiatedEvent extends DomainEvent {

    public static final String EVENT_TYPE = "PAYMENT_INITIATED";

    private final String transactionId;
    private final String phoneNumber;
    private final BigDecimal amount;
    private final String currency;
    private final String operatorCode;
    private final String idempotencyKey;

    public PaymentInitiatedEvent(
            String correlationId,
            TransactionId transactionId,
            PhoneNumber phoneNumber,
            Money money,
            OperatorCode operatorCode,
            IdempotencyKey idempotencyKey) {
        super(correlationId);
        this.transactionId = transactionId.toString();
        this.phoneNumber = phoneNumber.value();
        this.amount = money.amount();
        this.currency = money.currencyCode();
        this.operatorCode = operatorCode.name();
        this.idempotencyKey = idempotencyKey.value();
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public String getAggregateId() {
        return transactionId;
    }

    public String getTransactionId() { return transactionId; }
    public String getPhoneNumber() { return phoneNumber; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getOperatorCode() { return operatorCode; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
