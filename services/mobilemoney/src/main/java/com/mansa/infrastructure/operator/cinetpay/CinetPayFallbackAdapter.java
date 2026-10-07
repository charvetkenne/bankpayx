package com.mansa.infrastructure.operator.cinetpay;



import com.mansa.application.port.out.MobileMoneyOperatorPort;
import com.mansa.domain.valueobject.TransactionStatus;
import com.mansa.domain.execption.OperatorUnavailableException;
import com.mansa.domain.valueobject.Money;
import com.mansa.domain.valueobject.OperatorCode;
import com.mansa.domain.valueobject.OperatorReference;
import com.mansa.domain.valueobject.PhoneNumber;
import com.mansa.domain.valueobject.TransactionId;
import com.mansa.infrastructure.operator.cinetpay.dto.CinetPayPaymentResponse;

import com.mansa.infrastructure.operator.cinetpay.config.CinetPayProperties;
import com.mansa.infrastructure.operator.cinetpay.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CinetPayFallbackAdapter implements MobileMoneyOperatorPort {

    private final CinetPayApiClient cinetPayApiClient;
    private final CinetPayProperties cinetPayProperties;

    @Override
    public OperatorCode getSupportedOperator() {
        return OperatorCode.CINETPAY;
    }

    @Override
    public PaymentResult initiatePayment(
            TransactionId transactionId,
            PhoneNumber phoneNumber,
            Money amount,
            String correlationId) {

        CinetPayPaymentRequest request = CinetPayPaymentRequest.builder()
                .apiKey(cinetPayProperties.apiKey())
                .siteId(cinetPayProperties.siteId())
                .transactionId(transactionId.toString())
                .amount(amount.amount().intValue())
                .currency(amount.currencyCode())
                .description("BankPayX Mobile Money via CinetPay - " + transactionId)
                .returnUrl(cinetPayProperties.returnUrl())
                .notifyUrl(cinetPayProperties.callbackUrl())
                .customerPhoneNumber(phoneNumber.localNumber())
                .channels("MOBILE_MONEY")
                .metadata(correlationId)
                .build();

        try {
            CinetPayPaymentResponse response = cinetPayApiClient.initiatePayment(request);

            if (response != null && "201".equals(response.code()) && response.data() != null) {
                String paymentToken = response.data().paymentToken();
                log.info("CinetPay payment initiated: txId={}, token={}", transactionId, paymentToken);
                return PaymentResult.success(
                        OperatorReference.of(paymentToken),
                        "PENDING",
                        "CinetPay payment link created: " + response.data().paymentUrl()
                );
            }

            String msg = response != null ? response.message() : "Null response from CinetPay";
            log.error("CinetPay payment failed: txId={}, msg={}", transactionId, msg);
            return PaymentResult.failure("CINETPAY_ERROR", msg);

        } catch (Exception e) {
            log.error("CinetPay adapter error: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.CINETPAY, e.getMessage(), e);
        }
    }

    @Override
    public StatusResult checkPaymentStatus(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {

        try {
            CinetPayStatusResponse response = cinetPayApiClient.checkPayment(
                    cinetPayProperties.apiKey(),
                    cinetPayProperties.siteId(),
                    transactionId.toString()
            );

            if (response == null || response.data() == null) {
                return StatusResult.of(TransactionStatus.PROCESSING, null, "UNKNOWN", "No response");
            }

            TransactionStatus status = switch (response.data().status().toUpperCase()) {
                case "ACCEPTED" -> TransactionStatus.SUCCEEDED;
                case "REFUSED" -> TransactionStatus.FAILED;
                case "CANCELLED" -> TransactionStatus.CANCELLED;
                default -> TransactionStatus.PROCESSING;
            };

            OperatorReference opRef = response.data().operatorId() != null
                    ? OperatorReference.of(response.data().operatorId()) : null;

            return StatusResult.of(status, opRef, response.data().status(), response.message());

        } catch (Exception e) {
            log.error("CinetPay status failed: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.CINETPAY, e.getMessage(), e);
        }
    }

    @Override
    public boolean cancelPayment(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {
        log.info("CinetPay does not support explicit cancellation via API: txId={}", transactionId);
        return false;
    }
}