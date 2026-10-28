package com.mansa.application.port.in;

import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.model.CardPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;

public interface GetCardTransactionHistoryUseCase {

    record Filter(
            String        merchantId,
            PaymentStatus status,
            Instant       from,
            Instant       to
    ) {
        public static Filter empty() { return new Filter(null, null, null, null); }
    }

    Page<CardPayment> getHistory(Filter filter, Pageable pageable);
}
