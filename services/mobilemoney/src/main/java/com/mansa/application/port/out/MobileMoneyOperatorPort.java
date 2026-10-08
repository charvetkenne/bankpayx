package com.mansa.application.port.out;



import com.mansa.domain.valueobject.*;

import java.util.Optional;

public interface MobileMoneyOperatorPort {

    OperatorCode getSupportedOperator();

    PaymentResult initiatePayment(
            TransactionId transactionId,
            PhoneNumber phoneNumber,
            Money amount,
            String correlationId
    );

    StatusResult checkPaymentStatus(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId
    );

    boolean cancelPayment(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId
    );

    record PaymentResult(
            boolean success,
            OperatorReference operatorReference,
            String resultCode,
            String resultMessage
    ) {
        public static PaymentResult success(OperatorReference ref, String code, String message) {
            return new PaymentResult(true, ref, code, message);
        }

        public static PaymentResult failure(String code, String message) {
            return new PaymentResult(false, null, code, message);
        }
    }

    record StatusResult(
            TransactionStatus status,
            Optional<OperatorReference> operatorReference,
            String resultCode,
            String resultMessage
    ) {
        public static StatusResult of(TransactionStatus status, OperatorReference ref,
                                      String code, String message) {
            return new StatusResult(status, Optional.ofNullable(ref), code, message);
        }
    }
}
