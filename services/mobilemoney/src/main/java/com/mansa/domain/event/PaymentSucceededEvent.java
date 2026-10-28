package com.mansa.domain.event;


import java.math.BigDecimal;
import java.time.Instant;

import com.mansa.domain.valueobject.*;

public final class PaymentSucceededEvent extends DomainEvent {

    public static final String EVENT_TYPE = "PAYMENT_SUCCEEDED";

    private final String transactionId;
    private final String operatorReference;
    private final BigDecimal amount;
    private final String currency;
    private final String operatorCode;
    private final Instant succeededAt;

    public PaymentSucceededEvent(
            String correlationId,
            TransactionId transactionId,
            OperatorReference operatorReference,
            Money money,
            OperatorCode operatorCode,
            Instant succeededAt) {
        super(correlationId);
        this.transactionId = transactionId.toString();
        this.operatorReference = operatorReference.value();
        this.amount = money.amount();
        this.currency = money.currencyCode();
        this.operatorCode = operatorCode.name();
        this.succeededAt = succeededAt;
    }

    @Override
    public String getEventType() { return EVENT_TYPE; }

    @Override
    public String getAggregateId() { return transactionId; }

    public String getTransactionId() { return transactionId; }
    public String getOperatorReference() { return operatorReference; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getOperatorCode() { return operatorCode; }
    public Instant getSucceededAt() { return succeededAt; }
}
