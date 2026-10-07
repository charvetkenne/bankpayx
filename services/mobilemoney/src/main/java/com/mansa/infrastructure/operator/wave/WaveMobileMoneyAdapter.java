package com.mansa.infrastructure.operator.wave;



import com.mansa.application.port.out.MobileMoneyOperatorPort;
import com.mansa.domain.execption.OperatorUnavailableException;
import com.mansa.domain.valueobject.*;
import com.mansa.infrastructure.operator.wave.config.WaveProperties;
import com.mansa.infrastructure.operator.wave.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaveMobileMoneyAdapter implements MobileMoneyOperatorPort {

    private final WaveApiClient waveApiClient;
    private final WaveProperties waveProperties;

    @Override
    public OperatorCode getSupportedOperator() {
        return OperatorCode.WAVE;
    }

    @Override
    public PaymentResult initiatePayment(
            TransactionId transactionId,
            PhoneNumber phoneNumber,
            Money amount,
            String correlationId) {

        WavePaymentRequest request = WavePaymentRequest.builder()
                .currency(amount.currencyCode())
                .amount(amount.amount().toPlainString())
                .errorUrl(waveProperties.callbackUrl() + "/error")
                .successUrl(waveProperties.callbackUrl() + "/success")
                .clientReference(transactionId.toString())
                .build();

        try {
            WavePaymentRequest response = waveApiClient.checkout(request);

            if (response != null && response.id() != null) {
                log.info("Wave checkout created: txId={}, waveId={}", transactionId, response.id());
                return PaymentResult.success(
                        OperatorReference.of(response.id()),
                        "PENDING",
                        "Wave checkout session created"
                );
            }
            return PaymentResult.failure("WAVE_NULL_RESPONSE", "Wave returned null session");

        } catch (Exception e) {
            log.error("Wave adapter error: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.WAVE, e.getMessage(), e);
        }
    }

    @Override
    public StatusResult checkPaymentStatus(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {

        try {
            WaveStatusResponse response = waveApiClient.getCheckoutStatus(operatorReference.value());

            if (response == null) {
                return StatusResult.of(TransactionStatus.PROCESSING, null, "UNKNOWN", "No response");
            }

            TransactionStatus status = switch (response.checkoutStatus().toUpperCase()) {
                case "COMPLETE" -> TransactionStatus.SUCCEEDED;
                case "CANCELLED" -> TransactionStatus.CANCELLED;
                case "EXPIRED" -> TransactionStatus.EXPIRED;
                default -> TransactionStatus.PROCESSING;
            };

            OperatorReference txnRef = response.transactionId() != null
                    ? OperatorReference.of(response.transactionId()) : null;

            return StatusResult.of(status, txnRef, response.checkoutStatus(), "Wave status: " + response.checkoutStatus());

        } catch (Exception e) {
            log.error("Wave status failed: txId={}, error={}", transactionId, e.getMessage());
            throw new com.mansa.domain.execption.OperatorUnavailableException(OperatorCode.WAVE, e.getMessage(), e);
        }
    }

    @Override
    public boolean cancelPayment(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {
        log.info("Wave cancellation is handled via checkout session expiry: txId={}", transactionId);
        return false;
    }
}
