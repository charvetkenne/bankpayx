package com.mansa.application.port.in;

import com.mansa.domain.model.PayPalOrder;

public interface CapturePayPalOrderUseCase {

    record Command(
            String orderId,        // PayPal ORDER ID reçu via returnUrl?token=ORDER_ID
            String correlationId
    ) {}

    PayPalOrder execute(Command command);
}
