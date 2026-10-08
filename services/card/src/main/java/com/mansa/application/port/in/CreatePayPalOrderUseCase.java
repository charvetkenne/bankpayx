package com.mansa.application.port.in;

import com.mansa.domain.model.PayPalOrder;

public interface CreatePayPalOrderUseCase {

    record Command(
            Double  amount,
            String  currency,
            String  merchantId,
            String  description,
            String  returnUrl,
            String  cancelUrl,
            String  correlationId
    ) {}

    PayPalOrder execute(Command command);
}
