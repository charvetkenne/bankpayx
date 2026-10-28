package com.mansa.infrastructure.operator.orange;


import org.springframework.stereotype.Component;

import com.mansa.application.port.out.MobileMoneyOperatorPort;
import com.mansa.domain.execption.OperatorUnavailableException;
//import com.mansa.domain.exception.OperatorUnavailableException;
import com.mansa.domain.valueobject.*;
import com.mansa.infrastructure.operator.orange.config.OrangeProperties;
import com.mansa.infrastructure.operator.orange.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mansa.domain.valueobject.TransactionId;

import com.mansa.domain.valueobject.PhoneNumber;

import com.mansa.domain.valueobject.Money;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrangeMobileMoneyAdapter implements MobileMoneyOperatorPort {
//com.mansa.application.port.out.MobileMoneyOperatorPort
    private final OrangeApiClient orangeApiClient;
    private final OrangeProperties orangeProperties;
//com.mansa.infrastructure.operator.orange.config.OrangeProperties
    @Override
    public OperatorCode getSupportedOperator() {
        return OperatorCode.ORANGE;
    }
//com.mansa.infrastructure.operator.orange.config.OrangeProperties
    @Override
    public PaymentResult initiatePayment(
            TransactionId transactionId,
            PhoneNumber phoneNumber,
            Money amount,
            String correlationId) {

        OrangePaymentRequest request = OrangePaymentRequest.builder()
                .merchantKey(orangeProperties.merchantId())
                .currency(amount.currencyCode())
                .orderId(transactionId.toString())
                .amount(amount.amount().intValue())
                .returnUrl(orangeProperties.callbackUrl() + "/success")
                .cancelUrl(orangeProperties.callbackUrl() + "/cancel")
                .notifUrl(orangeProperties.callbackUrl() + "/notify")
                .lang("fr")
                .reference(correlationId)
                .build();

        try {
            OrangePaymentResponse response = orangeApiClient.initiatePayment(request);

            if (response != null && "SUCCESS".equalsIgnoreCase(response.status()) && response.data() != null) {
                log.info("Orange payment initiated: txId={}, orangeId={}", transactionId, response.data().id());
                return PaymentResult.success(
                        OperatorReference.of(response.data().id()),
                        "PENDING",
                        "Orange Money payment initiated"
                );
            } else {
                String msg = response != null ? response.message() : "Null response from Orange";
                log.error("Orange payment failed: txId={}, msg={}", transactionId, msg);
                return PaymentResult.failure("ORANGE_ERROR", msg);
            }
        } catch (Exception e) {
            log.error("Orange adapter error: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.ORANGE, e.getMessage(), e);
        }
    }

    @Override
    public StatusResult checkPaymentStatus(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {

        try {
            OrangeStatusResponse response = orangeApiClient.getPaymentStatus(operatorReference.value());

            if (response == null || response.data() == null) {
                return StatusResult.of(TransactionStatus.PROCESSING, null, "UNKNOWN", "No response");
            }

            TransactionStatus status = switch (response.data().status().toUpperCase()) {
                case "SUCCESS", "SUCCESSFUL" -> TransactionStatus.SUCCEEDED;
                case "FAILED", "FAILURE" -> TransactionStatus.FAILED;
                case "EXPIRED" -> TransactionStatus.EXPIRED;
                default -> TransactionStatus.PROCESSING;
            };

            OperatorReference txnRef = response.data().txnid() != null
                    ? OperatorReference.of(response.data().txnid()) : null;

            return StatusResult.of(status, txnRef,
                    response.data().status(), response.data().message());

        } catch (Exception e) {
            log.error("Orange status check failed: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.ORANGE, e.getMessage(), e);
        }
    }

    @Override
    public boolean cancelPayment(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {
        log.info("Orange Money cancellation requested: txId={}", transactionId);
        return false;
    }
}
