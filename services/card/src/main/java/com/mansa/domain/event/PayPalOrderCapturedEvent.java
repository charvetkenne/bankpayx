package com.mansa.domain.event;

import com.mansa.domain.model.Money;

public class PayPalOrderCapturedEvent extends DomainEvent {

    private final String transactionId;
    private final String paypalOrderId;
    private final String captureId;
    private final Money  amount;

    public PayPalOrderCapturedEvent(String transactionId, String paypalOrderId,
                                    String captureId, Money amount, String correlationId) {
        super(correlationId);
        this.transactionId = transactionId;
        this.paypalOrderId = paypalOrderId;
        this.captureId     = captureId;
        this.amount        = amount;
    }

    public String getTransactionId() { return transactionId; }
    public String getPaypalOrderId() { return paypalOrderId; }
    public String getCaptureId()     { return captureId; }
    public Money  getAmount()        { return amount; }
}
