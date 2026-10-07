package com.mansa.infrastructure.operator.notchpay;



import com.mansa.application.port.out.MobileMoneyOperatorPort;
import com.mansa.domain.execption.OperatorUnavailableException;
import com.mansa.domain.valueobject.*;
import com.mansa.infrastructure.operator.notchpay.config.NotchPayProperties;
import com.mansa.infrastructure.operator.notchpay.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotchPayMobileMoneyAdapter implements MobileMoneyOperatorPort {

    /*
     * NotchPay est un agrégateur multi-opérateurs (MTN, Orange, Airtel…).
     * Il expose une API unifiée : on initialise un paiement et NotchPay
     * détermine lui-même l'opérateur sous-jacent selon le numéro de téléphone.
     * Le client est redirigé vers l'authorization_url pour finaliser.
     */

    private static final String NOTCHPAY_PENDING_EMAIL = "noreply@bankpayx.com";

    private final NotchPayApiClient notchPayApiClient;
    private final NotchPayProperties notchPayProperties;

    @Override
    public OperatorCode getSupportedOperator() {
        return OperatorCode.NOTCHPAY;
    }

    @Override
    public PaymentResult initiatePayment(
            TransactionId transactionId,
            PhoneNumber phoneNumber,
            Money amount,
            String correlationId) {

        log.info("NotchPay initiatePayment: txId={}, phone={}, amount={}",
                transactionId, phoneNumber, amount);

        /*
         * NotchPay exige un email client. En contexte mobile money pur,
         * on génère un email synthétique traceable par correlationId.
         * L'email réel peut être injecté via metadata si disponible.
         */
        String syntheticEmail = buildSyntheticEmail(phoneNumber, correlationId);

        NotchPayInitRequest request = NotchPayInitRequest.builder()
                .amount(amount.amount().intValue())
                .currency(amount.currencyCode())
                .email(syntheticEmail)
                .phone(phoneNumber.value())
                .reference(buildReference(transactionId))
                .description("BankPayX Mobile Money Payment - " + transactionId)
                .callback(notchPayProperties.callbackUrl())
                .locked(true)   // Verrouille le canal sur mobile money
                .build();

        try {
            NotchPayInitResponse response = notchPayApiClient.initializePayment(request);

            if (response == null || response.transaction() == null) {
                log.error("NotchPay returned null response: txId={}", transactionId);
                return PaymentResult.failure("NOTCHPAY_NULL_RESPONSE", "NotchPay returned null response");
            }

            String notchPayReference = response.transaction().reference();

            log.info("NotchPay payment initialized: txId={}, notchPayRef={}, status={}, authUrl={}",
                    transactionId, notchPayReference,
                    response.transaction().status(),
                    response.authorizationUrl());

            /*
             * On retourne la référence NotchPay comme OperatorReference.
             * Le message inclut l'authorization_url pour que l'application
             * puisse rediriger le client vers la page de paiement NotchPay.
             */
            return PaymentResult.success(
                    OperatorReference.of(notchPayReference),
                    response.transaction().status().toUpperCase(),
                    "NotchPay payment initialized. Authorization URL: " + response.authorizationUrl()
            );

        } catch (Exception e) {
            log.error("NotchPay initiatePayment failed: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.NOTCHPAY, e.getMessage(), e);
        }
    }

    @Override
    public StatusResult checkPaymentStatus(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {

        log.debug("NotchPay checkPaymentStatus: txId={}, notchPayRef={}",
                transactionId, operatorReference);

        try {
            NotchPayVerifyResponse response = notchPayApiClient.verifyPayment(operatorReference.value());

            if (response == null || response.transaction() == null) {
                log.warn("NotchPay returned null on verify: txId={}", transactionId);
                return StatusResult.of(
                        TransactionStatus.PROCESSING, null,
                        "NOTCHPAY_NO_RESPONSE", "No response from NotchPay"
                );
            }

            NotchPayVerifyResponse.NotchPayTransactionDetail tx = response.transaction();
            TransactionStatus domainStatus = mapNotchPayStatus(tx.status());

            /*
             * NotchPay retourne la même "reference" qu'à l'init.
             * On l'utilise comme référence finale confirmée.
             */
            OperatorReference confirmedRef = tx.reference() != null
                    ? OperatorReference.of(tx.reference())
                    : null;

            log.info("NotchPay status verified: txId={}, notchPayStatus={}, domainStatus={}, channel={}",
                    transactionId, tx.status(), domainStatus, tx.channel());

            return StatusResult.of(
                    domainStatus,
                    confirmedRef,
                    tx.status().toUpperCase(),
                    buildStatusMessage(tx)
            );

        } catch (Exception e) {
            log.error("NotchPay checkPaymentStatus failed: txId={}, error={}", transactionId, e.getMessage());
            throw new OperatorUnavailableException(OperatorCode.NOTCHPAY, e.getMessage(), e);
        }
    }

    @Override
    public boolean cancelPayment(
            TransactionId transactionId,
            OperatorReference operatorReference,
            String correlationId) {

        log.info("NotchPay cancelPayment: txId={}, notchPayRef={}", transactionId, operatorReference);

        try {
            return notchPayApiClient.cancelPayment(operatorReference.value());
        } catch (Exception e) {
            log.error("NotchPay cancelPayment failed: txId={}, error={}", transactionId, e.getMessage());
            return false;
        }
    }

    // ---- Helpers privés ----

    /**
     * Mappe les statuts NotchPay vers les statuts du domaine BankPayX.
     *
     * Statuts NotchPay : pending | processing | complete | failed | canceled | expired
     */
    private TransactionStatus mapNotchPayStatus(String notchPayStatus) {
        if (notchPayStatus == null) return TransactionStatus.PROCESSING;
        return switch (notchPayStatus.toLowerCase()) {
            case "complete"    -> TransactionStatus.SUCCEEDED;
            case "failed"      -> TransactionStatus.FAILED;
            case "canceled"    -> TransactionStatus.CANCELLED;
            case "expired"     -> TransactionStatus.EXPIRED;
            case "pending"     -> TransactionStatus.PENDING;
            case "processing"  -> TransactionStatus.PROCESSING;
            default            -> TransactionStatus.PROCESSING;
        };
    }

    /**
     * Construit la référence unique NotchPay à partir du TransactionId BankPayX.
     * Format : "bankpayx-{uuid}" — doit être unique par transaction.
     */
    private String buildReference(TransactionId transactionId) {
        return "bankpayx-" + transactionId.value().toString().replace("-", "").substring(0, 20);
    }

    /**
     * Construit un email synthétique unique par téléphone + correlationId.
     * Nécessaire car NotchPay exige un email pour chaque transaction.
     */
    private String buildSyntheticEmail(PhoneNumber phoneNumber, String correlationId) {
        String localPart = phoneNumber.localNumber().replaceAll("[^0-9]", "");
        return localPart + "." + correlationId.substring(0, Math.min(8, correlationId.length()))
                + "@" + NOTCHPAY_PENDING_EMAIL.split("@")[1];
    }

    private String buildStatusMessage(NotchPayVerifyResponse.NotchPayTransactionDetail tx) {
        return String.format("NotchPay status: %s | channel: %s | ref: %s",
                tx.status(),
                tx.channel() != null ? tx.channel() : "N/A",
                tx.reference());
    }
}
