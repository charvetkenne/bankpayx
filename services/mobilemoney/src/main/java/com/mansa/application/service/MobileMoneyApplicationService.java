package com.mansa.application.service;



import com.mansa.application.port.in.*;
import com.mansa.application.port.out.*;
import com.mansa.application.usecase.*;
import com.mansa.domain.aggregate.MobileMoneyTransaction;
// import com.mansa.domain.aggregate.MobileMoneyTransaction;
import com.mansa.domain.entity.TransactionAttempt;
import com.mansa.domain.event.DomainEvent;
import com.mansa.domain.execption.InvalidTransactionStateException;
import com.mansa.domain.execption.OperatorUnavailableException;
import com.mansa.domain.execption.TransactionNotFoundException;
import com.mansa.domain.service.TransactionDomainService;
import com.mansa.domain.valueobject.*;
import com.mansa.infrastructure.monitoring.MobileMoneyMetrics;

// import com.mansa.infrastructure.monitoring.MobileMoneyMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileMoneyApplicationService
        implements InitiatePaymentPort,
                   CheckTransactionStatusPort,
                   HandleCallbackPort,
                   RetryTransactionPort,
                   CancelTransactionPort {

    private static final long IDEMPOTENCY_TTL_SECONDS = 86_400L; // 24h
    private static final String TOPIC_PAYMENT_EVENTS = "payment-events";

    private final TransactionRepositoryPort transactionRepository;
    private final OutboxRepositoryPort outboxRepository;
    private final IdempotencyPort idempotencyPort;
    private final TransactionDomainService transactionDomainService;
    private final MobileMoneyMetrics metrics;

    /**
     * Map of operator code -> operator adapter, injected by Spring.
     * This is the Open/Closed-compliant operator routing mechanism.
     */
    private final Map<OperatorCode, MobileMoneyOperatorPort> operatorAdapters;

    // ========================================================
    // INITIATE PAYMENT
    // ========================================================

    @Override
    @Transactional
    public InitiatePaymentUseCase.InitiatePaymentResult initiatePayment(
            InitiatePaymentUseCase.InitiatePaymentCommand command) {

        log.info("Initiating payment: correlationId={}, idempotencyKey={}, operator={}",
                command.correlationId(), command.idempotencyKey(), command.operatorCode());

        IdempotencyKey idempotencyKey = IdempotencyKey.of(command.idempotencyKey());

        // --- Idempotency check ---
        Optional<TransactionId> existingId = idempotencyPort.getIfPresent(idempotencyKey);
        if (existingId.isPresent()) {
            MobileMoneyTransaction existing = transactionRepository.findById(existingId.get())
                    .orElseThrow(() -> new TransactionNotFoundException(existingId.get().toString()));
            log.warn("Duplicate request detected: idempotencyKey={}, existingTxId={}",
                    idempotencyKey, existing.getId());
            return new InitiatePaymentUseCase.InitiatePaymentResult(
                    existing.getId().toString(),
                    existing.getStatus(),
                    existing.getOperatorCode().name(),
                    true
            );
        }

        // --- Build domain aggregate ---
        OperatorCode operatorCode = OperatorCode.fromIdentifier(command.operatorCode());
        PhoneNumber phoneNumber = PhoneNumber.of(command.phoneNumber());
        Money amount = Money.of(command.amount(), command.currencyCode());

        MobileMoneyTransaction transaction = MobileMoneyTransaction.initiate(
                phoneNumber, amount, operatorCode,
                command.customerId(), idempotencyKey, command.correlationId()
        );

        // --- Store idempotency key ---
        idempotencyPort.store(idempotencyKey, transaction.getId(), IDEMPOTENCY_TTL_SECONDS);

        // --- Save transaction (INITIATED) ---
        transactionRepository.save(transaction);

        // --- Call operator ---
        transaction.markAsPending();
        transactionRepository.save(transaction);

        TransactionAttempt attempt = transaction.recordAttempt();

        try {
            MobileMoneyOperatorPort operator = resolveOperator(operatorCode);
            MobileMoneyOperatorPort.PaymentResult result = operator.initiatePayment(
                    transaction.getId(),
                    transaction.getPhoneNumber(),
                    transaction.getAmount(),
                    command.correlationId()
            );

            if (result.success()) {
                attempt.markSuccess(result.resultCode(), result.resultMessage());
                transaction.markAsProcessing();
                log.info("Payment submitted to operator: txId={}, operatorRef={}",
                        transaction.getId(), result.operatorReference());
                metrics.recordPaymentInitiated(operatorCode.name());
            } else {
                attempt.markFailure(result.resultCode(), result.resultMessage());
                transaction.markAsFailed(result.resultCode(), result.resultMessage());
                log.warn("Operator rejected payment: txId={}, code={}, msg={}",
                        transaction.getId(), result.resultCode(), result.resultMessage());
                metrics.recordPaymentFailed(operatorCode.name(), result.resultCode());
            }

        } catch (OperatorUnavailableException e) {
            log.warn("Operator {} unavailable, switching to CinetPay fallback: txId={}",
                    operatorCode, transaction.getId());

            OperatorCode fallback = transactionDomainService.determineFallbackOperator(operatorCode);
            transaction.switchToFallbackOperator(fallback);
            attempt.markFailure("OPERATOR_UNAVAILABLE", e.getMessage());

            MobileMoneyOperatorPort fallbackOperator = resolveOperator(fallback);
            MobileMoneyOperatorPort.PaymentResult fallbackResult = fallbackOperator.initiatePayment(
                    transaction.getId(),
                    transaction.getPhoneNumber(),
                    transaction.getAmount(),
                    command.correlationId()
            );

            if (fallbackResult.success()) {
                transaction.markAsProcessing();
                metrics.recordPaymentInitiated(fallback.name());
            } else {
                transaction.markAsFailed(fallbackResult.resultCode(), fallbackResult.resultMessage());
                metrics.recordPaymentFailed(fallback.name(), fallbackResult.resultCode());
            }
        }

        // --- Persist final state ---
        transactionRepository.save(transaction);

        // --- Drain and save domain events to outbox ---
        publishDomainEvents(transaction);

        return new InitiatePaymentUseCase.InitiatePaymentResult(
                transaction.getId().toString(),
                transaction.getStatus(),
                transaction.getOperatorCode().name(),
                false
        );
    }

    // ========================================================
    // CHECK STATUS
    // ========================================================

    @Override
    @Transactional(readOnly = true)
    public CheckTransactionStatusUseCase.TransactionStatusResult checkStatus(String transactionId) {
        MobileMoneyTransaction tx = findTransactionOrThrow(transactionId);

        return new CheckTransactionStatusUseCase.TransactionStatusResult(
                tx.getId().toString(),
                tx.getStatus(),
                tx.getOperatorCode().name(),
                tx.getOperatorReference() != null ? tx.getOperatorReference().value() : null,
                tx.getFailureReason(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }

    // ========================================================
    // HANDLE CALLBACK
    // ========================================================

    @Override
    @Transactional
    public void handleCallback(HandleCallbackUseCase.CallbackCommand command) {
        log.info("Handling callback: txId={}, operator={}, status={}",
                command.transactionId(), command.operatorCode(), command.status());

        MobileMoneyTransaction transaction = findTransactionOrThrow(command.transactionId());

        if (transaction.isTerminal()) {
            log.warn("Callback received for terminal transaction, ignoring: txId={}",
                    command.transactionId());
            return;
        }

        switch (command.status().toUpperCase()) {
            case "SUCCESS", "SUCCESSFUL", "COMPLETED" -> {
                OperatorReference ref = OperatorReference.of(command.operatorReference());
                transaction.markAsSucceeded(ref);
                metrics.recordPaymentSucceeded(command.operatorCode());
                log.info("Transaction succeeded via callback: txId={}", command.transactionId());
            }
            case "FAILED", "FAILURE", "ERROR" -> {
                transaction.markAsFailed(
                        command.failureCode() != null ? command.failureCode() : "OPERATOR_FAILURE",
                        command.failureMessage() != null ? command.failureMessage() : "Operator reported failure"
                );
                metrics.recordPaymentFailed(command.operatorCode(),
                        command.failureCode() != null ? command.failureCode() : "OPERATOR_FAILURE");
                log.warn("Transaction failed via callback: txId={}, code={}",
                        command.transactionId(), command.failureCode());
            }
            default -> log.warn("Unknown callback status '{}' for txId={}, ignoring",
                    command.status(), command.transactionId());
        }

        transactionRepository.save(transaction);
        publishDomainEvents(transaction);
    }

    // ========================================================
    // RETRY TRANSACTION
    // ========================================================

    @Override
    @Transactional
    public RetryTransactionUseCase.RetryResult retryTransaction(String transactionId) {
        log.info("Retrying transaction: txId={}", transactionId);

        MobileMoneyTransaction transaction = findTransactionOrThrow(transactionId);

        if (!transaction.canRetry()) {
            throw new InvalidTransactionStateException(
                    transaction.getStatus(), TransactionStatus.INITIATED
            );
        }

        OperatorCode retryOperator = transactionDomainService.determineRetryOperator(transaction);
        transaction.retryWith(retryOperator);
        transaction.markAsPending();
        transactionRepository.save(transaction);

        TransactionAttempt attempt = transaction.recordAttempt();

        try {
            MobileMoneyOperatorPort operator = resolveOperator(retryOperator);
            MobileMoneyOperatorPort.PaymentResult result = operator.initiatePayment(
                    transaction.getId(),
                    transaction.getPhoneNumber(),
                    transaction.getAmount(),
                    transaction.getCorrelationId()
            );

            if (result.success()) {
                attempt.markSuccess(result.resultCode(), result.resultMessage());
                transaction.markAsProcessing();
                metrics.recordPaymentInitiated(retryOperator.name());
            } else {
                attempt.markFailure(result.resultCode(), result.resultMessage());
                transaction.markAsFailed(result.resultCode(), result.resultMessage());
                metrics.recordPaymentFailed(retryOperator.name(), result.resultCode());
            }
        } catch (OperatorUnavailableException e) {
            attempt.markFailure("OPERATOR_UNAVAILABLE", e.getMessage());
            transaction.markAsFailed("OPERATOR_UNAVAILABLE", e.getMessage());
            metrics.recordPaymentFailed(retryOperator.name(), "OPERATOR_UNAVAILABLE");
        }

        transactionRepository.save(transaction);
        publishDomainEvents(transaction);

        return new RetryTransactionUseCase.RetryResult(
                transaction.getId().toString(),
                transaction.getStatus(),
                transaction.getOperatorCode().name(),
                transaction.getRetryCount()
        );
    }

    // ========================================================
    // CANCEL TRANSACTION
    // ========================================================

    @Override
    @Transactional
    public void cancelTransaction(CancelTransactionUseCase.CancelCommand command) {
        log.info("Cancelling transaction: txId={}, reason={}", command.transactionId(), command.cancellationReason());

        MobileMoneyTransaction transaction = findTransactionOrThrow(command.transactionId());
        transaction.cancel(command.cancellationReason());
        transactionRepository.save(transaction);
        publishDomainEvents(transaction);

        metrics.recordPaymentCancelled(transaction.getOperatorCode().name());
        log.info("Transaction cancelled: txId={}", command.transactionId());
    }

    // ========================================================
    // PRIVATE HELPERS
    // ========================================================

    private MobileMoneyTransaction findTransactionOrThrow(String transactionId) {
        return transactionRepository
                .findById(TransactionId.of(transactionId))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    private MobileMoneyOperatorPort resolveOperator(OperatorCode operatorCode) {
        MobileMoneyOperatorPort adapter = operatorAdapters.get(operatorCode);
        if (adapter == null) {
            throw new OperatorUnavailableException(operatorCode,
                    "No adapter registered for operator: " + operatorCode);
        }
        return adapter;
    }

    private void publishDomainEvents(MobileMoneyTransaction transaction) {
        List<DomainEvent> events = transaction.pullDomainEvents();
        for (DomainEvent event : events) {
            outboxRepository.saveOutboxMessage(event, TOPIC_PAYMENT_EVENTS);
            log.debug("Outbox message saved: eventType={}, txId={}",
                    event.getEventType(), event.getAggregateId());
        }
    }
}
