package com.mansa.application.service;


import com.mansa.application.port.out.MobileMoneyOperatorPort;
import com.mansa.application.port.out.TransactionRepositoryPort;
import com.mansa.domain.aggregate.MobileMoneyTransaction;
import com.mansa.domain.execption.TransactionNotFoundException;
import com.mansa.domain.valueobject.OperatorCode;
import com.mansa.domain.valueobject.TransactionId;
import com.mansa.domain.valueobject.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionRepositoryPort transactionRepository;
    private final Map<OperatorCode, MobileMoneyOperatorPort> operatorAdapters;

    /**
     * Reconciles transaction status by polling the operator directly.
     * Used for pending transactions where callback hasn't been received.
     */
    @Transactional
    public MobileMoneyTransaction reconcileStatus(String transactionId) {
        MobileMoneyTransaction transaction = transactionRepository
                .findById(TransactionId.of(transactionId))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (transaction.isTerminal()) {
            log.debug("Transaction already in terminal state, skipping reconciliation: txId={}", transactionId);
            return transaction;
        }

        if (transaction.getOperatorReference() == null) {
            log.warn("Cannot reconcile: no operator reference for txId={}", transactionId);
            return transaction;
        }

        try {
            MobileMoneyOperatorPort operator = operatorAdapters.get(transaction.getOperatorCode());
            if (operator == null) {
                log.error("No adapter for operator {} during reconciliation", transaction.getOperatorCode());
                return transaction;
            }

            MobileMoneyOperatorPort.StatusResult statusResult = operator.checkPaymentStatus(
                    transaction.getId(),
                    transaction.getOperatorReference(),
                    transaction.getCorrelationId()
            );

            log.info("Reconciliation result: txId={}, operatorStatus={}", transactionId, statusResult.status());

            if (statusResult.status() == TransactionStatus.SUCCEEDED) {
                statusResult.operatorReference().ifPresent(transaction::markAsSucceeded);
                transactionRepository.save(transaction);
            } else if (statusResult.status() == TransactionStatus.FAILED) {
                transaction.markAsFailed(statusResult.resultCode(), statusResult.resultMessage());
                transactionRepository.save(transaction);
            }

        } catch (Exception e) {
            log.error("Reconciliation failed for txId={}: {}", transactionId, e.getMessage());
        }

        return transaction;
    }
}