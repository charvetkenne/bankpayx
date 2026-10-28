package com.mansa.application.port.in;

import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.model.PayPalOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;

public interface GetPayPalOrderHistoryUseCase {

    record Filter(
            String            merchantId,
            PayPalOrderStatus status,
            Instant           from,
            Instant           to
    ) {
        public static Filter empty() { return new Filter(null, null, null, null); }
    }

    Page<PayPalOrder> getHistory(Filter filter, Pageable pageable);
}
