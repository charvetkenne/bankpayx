package com.mansa.application.port.out;

import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.model.PayPalOrder;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayPalOrderPersistencePort {

    PayPalOrder save(PayPalOrder order);

    Optional<PayPalOrder> findByTransactionId(String transactionId);

    Optional<PayPalOrder> findByPaypalOrderId(String paypalOrderId);
//______________________________
     Page<PayPalOrder> findAll(Pageable pageable);

    Page<PayPalOrder> findByMerchantId(String merchantId, Pageable pageable);

    Page<PayPalOrder> findByFilters(String merchantId, PayPalOrderStatus status,
                                    Instant from, Instant to, Pageable pageable);
}
