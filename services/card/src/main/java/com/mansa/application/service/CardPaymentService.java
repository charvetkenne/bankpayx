package com.mansa.application.service;

import com.mansa.application.port.in.ProcessCardPaymentUseCase;
//import com.mansa.application.port.out.CardValidationPort;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.PaymentGatewayPort;
import com.mansa.application.port.out.TransactionPersistencePort;
import com.mansa.domain.enums.Currency;
//import com.mansa.domain.exception.InvalidCardException;
//import com.mansa.domain.model.Card;
import com.mansa.domain.model.CardPayment;
import com.mansa.domain.model.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.time.YearMonth;
//import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class CardPaymentService implements ProcessCardPaymentUseCase {

    private final PaymentGatewayPort         gatewayPort;
    private final TransactionPersistencePort persistencePort;
    private final EventPublisherPort         eventPublisherPort;

    @Override
    @Transactional
    public CardPayment process(Command command) {
        log.info("[{}] Processing payment: merchant={} amount={} {}",
                command.correlationId(), command.merchantId(),
                command.amount(), command.currency());

        Money money = Money.of(command.amount(), Currency.valueOf(command.currency()));

        // L'userId vient du JWT Supabase (claim "sub") : c'est un UUID.
        UUID userId = UUID.fromString(command.userId());

        CardPayment payment = CardPayment.initiate(
                userId, money, command.merchantId(), command.description(),
                command.correlationId(), command.paymentMethodId()
        );

        CardPayment saved = persistencePort.save(payment);

        try {
            String gatewayId = gatewayPort.authorize(saved);
            saved.markAuthorized(gatewayId);
            log.info("[{}] Payment authorized: transactionId={} gatewayId={}",
                    command.correlationId(), saved.getTransactionId(), gatewayId);

        } catch (Exception ex) {
            log.error("[{}] Payment failed: {}", command.correlationId(), ex.getMessage());
            saved.markFailed(ex.getMessage());
        }

        CardPayment updated = persistencePort.save(saved);
        eventPublisherPort.publishAll(updated.pullDomainEvents());
        return updated;
    }
}


// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class CardPaymentService implements ProcessCardPaymentUseCase {

//     private final PaymentGatewayPort         gatewayPort;
//     private final TransactionPersistencePort persistencePort;
//     private final EventPublisherPort         eventPublisherPort;

//     @Override
//     @Transactional
//     public CardPayment process(Command command) {
//         log.info("[{}] Processing payment: merchant={} amount={} {}",
//                 command.correlationId(), command.merchantId(),
//                 command.amount(), command.currency());

//         Money money = Money.of(command.amount(), Currency.valueOf(command.currency()));
//         CardPayment payment = CardPayment.initiate(
//                 money, command.merchantId(), command.description(),
//                 command.correlationId(), command.paymentMethodId()
//         );

//         CardPayment saved = persistencePort.save(payment);

//         try {
//             String gatewayId = gatewayPort.authorize(saved);
//             saved.markAuthorized(gatewayId);
//             log.info("[{}] Payment authorized: transactionId={} gatewayId={}",
//                     command.correlationId(), saved.getTransactionId(), gatewayId);

//         } catch (Exception ex) {
//             log.error("[{}] Payment failed: {}", command.correlationId(), ex.getMessage());
//             saved.markFailed(ex.getMessage());
//         }

//         CardPayment updated = persistencePort.save(saved);
//         eventPublisherPort.publishAll(updated.pullDomainEvents());
//         return updated;
//     }
// }

