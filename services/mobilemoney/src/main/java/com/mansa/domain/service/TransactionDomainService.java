package com.mansa.domain.service;


import com.mansa.domain.aggregate.MobileMoneyTransaction;
import com.mansa.domain.valueobject.OperatorCode;
import org.springframework.stereotype.Service;

@Service
public class TransactionDomainService {

    /**
     * Determines the fallback operator when the primary operator is unavailable.
     * CinetPay is the universal fallback for all operators.
     * Business rule: CinetPay is never its own fallback.
     */
    public OperatorCode determineFallbackOperator(OperatorCode primaryOperator) {
        if (primaryOperator == OperatorCode.CINETPAY) {
            throw new IllegalStateException(
                    "CinetPay is the fallback provider and cannot have a fallback itself"
            );
        }
        return OperatorCode.CINETPAY;
    }

    /**
     * Determines the retry operator for a failed transaction.
     * If the current operator is not CinetPay, we try CinetPay as fallback.
     * If already on CinetPay, we attempt again on CinetPay (already the fallback).
     */
    public OperatorCode determineRetryOperator(MobileMoneyTransaction transaction) {
        OperatorCode current = transaction.getOperatorCode();
        if (current != OperatorCode.CINETPAY) {
            return OperatorCode.CINETPAY;
        }
        return current;
    }

    /**
     * Validates business rules before initiating a payment.
     * Returns a validation failure message, or empty string if valid.
     */
    public String validateInitiation(MobileMoneyTransaction transaction) {
        if (transaction.getAmount().isZero()) {
            return "Transaction amount cannot be zero";
        }
        return "";
    }
}
