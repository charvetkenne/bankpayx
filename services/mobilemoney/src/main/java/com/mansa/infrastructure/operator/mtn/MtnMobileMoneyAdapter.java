package com.mansa.infrastructure.operator.mtn;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.mansa.application.port.out.MobileMoneyOperatorPort;
import com.mansa.domain.execption.OperatorUnavailableException;
import com.mansa.domain.valueobject.Money;
import com.mansa.domain.valueobject.OperatorCode;
import com.mansa.domain.valueobject.OperatorReference;
import com.mansa.domain.valueobject.PhoneNumber;
import com.mansa.domain.valueobject.TransactionId;
import com.mansa.domain.valueobject.TransactionStatus;
import com.mansa.infrastructure.operator.mtn.config.MtnProperties;
import com.mansa.infrastructure.operator.mtn.dto.MtnPaymentRequest;
import com.mansa.infrastructure.operator.mtn.dto.MtnPaymentResponse;
import com.mansa.infrastructure.operator.mtn.dto.MtnStatusResponse;

//import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MtnMobileMoneyAdapter implements MobileMoneyOperatorPort {

    private final MtnApiClient mtnApiClient;
    private final MtnProperties mtnProperties;

    @Override
    public OperatorCode getSupportedOperator() {
        return OperatorCode.MTN;
    }

    @Override
    public PaymentResult initiatePayment(
            TransactionId transactionId,
            PhoneNumber phoneNumber,
            Money amount,
            String correlationId) {

        String referenceId = UUID.randomUUID().toString();

        MtnPaymentRequest request = MtnPaymentRequest.builder()
                .amount(amount.amount().toPlainString())
                .currency(amount.currencyCode())
                .externalId(transactionId.toString())
                .payer(new MtnPaymentRequest.MtnPayer("MSISDN", phoneNumber.localNumber()))
                .payerMessage("BankPayX Payment " + transactionId)
                .payeeNote("Payment ref: " + correlationId)
                .build();

        try {
            MtnPaymentResponse response = mtnApiClient.requestToPay(request, referenceId);
            log.info("MTN payment initiated: txId={}, mtnRef={}", transactionId, referenceId);
            return PaymentResult.success(
                    OperatorReference.of(referenceId),
                    "PENDING",
                    "Payment request accepted by MTN"
            );
        } catch (Exception e) {
            log.error("MTN payment initiation failed: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.MTN, e.getMessage(), e);
        }
    }

    @Override
    public StatusResult checkPaymentStatus(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {

        try {
            MtnStatusResponse response = mtnApiClient.getRequestToPayStatus(operatorReference.value());

            TransactionStatus status = switch (response.status().toUpperCase()) {
                case "SUCCESSFUL" -> TransactionStatus.SUCCEEDED;
                case "FAILED" -> TransactionStatus.FAILED;
                default -> TransactionStatus.PROCESSING;
            };

            String code = response.reason() != null ? response.reason().code() : response.status();
            String message = response.reason() != null ? response.reason().message() : response.status();

            return StatusResult.of(status,
                    response.financialTransactionId() != null
                            ? OperatorReference.of(response.financialTransactionId())
                            : null,
                    code, message);

        } catch (Exception e) {
            log.error("MTN status check failed: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.MTN, e.getMessage(), e);
        }
    }

    @Override
    public boolean cancelPayment(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {
        // MTN MoMo API does not support explicit cancellation via API —
        // pending requests expire automatically after operator timeout.
        log.info("MTN does not support explicit cancellation: txId={}", transactionId);
        return false;
    }
}