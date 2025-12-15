package com.mansa.application.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansa.application.port.in.AuthorizeCardUseCase;
import com.mansa.application.port.out.PersistTransactionPort;
import com.mansa.domain.aggregate.CardAuthorization;
import com.mansa.domain.entity.CardTransaction;
import com.mansa.dto.AuthorizationRequestDTO;
import com.mansa.dto.AuthorizationResponseDTO;
import com.mansa.outbox.OutboxEvent;
import com.mansa.outbox.OutboxEventRepository;
import com.mansa.domain.event.AuthorizationResultEvent;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizeCardService implements AuthorizeCardUseCase {

    public static final String TOPIC = "card.authorizations";

    private final PersistTransactionPort persistPort;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public AuthorizeCardService(PersistTransactionPort persistPort,
                                OutboxEventRepository outboxRepo,
                                ObjectMapper objectMapper) {
        this.persistPort = persistPort;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AuthorizationResponseDTO authorize(AuthorizationRequestDTO request) {
        // 1) Build aggregate and evaluate decision (domain logic)
        CardAuthorization auth = new CardAuthorization(
                request.pan,
                request.amount,
                request.currency,
                request.merchantId,
                request.rrn
        );
        CardAuthorization.Decision decision = auth.authorize(request.expiry);
        CardTransaction tx = auth.getTransaction();

        // 2) Persist transaction (participates in same DB transaction)
        CardTransaction saved = persistPort.save(tx);

        // 3) Build domain event DTO
        AuthorizationResultEvent event = new AuthorizationResultEvent(
                saved.getId(),
                saved.getRrn(),
                "AUTHORIZED".equals(saved.getStatus()),
                saved.getAuthCode(),
                decision.reason,
                saved.getAmount(),
                saved.getCurrency(),
                saved.getResponseTime()
        );

        try {
            // 4) Serialize payload to JSON (ensure no clear PAN in payload)
            String payload = objectMapper.writeValueAsString(event);

            // 5) Write OutboxEvent in same DB transaction
            OutboxEvent outbox = new OutboxEvent(
                    "CardTransaction",
                    saved.getId(),
                    TOPIC,
                    saved.getId(),
                    payload
            );
            outboxRepo.save(outbox);

        } catch (Exception ex) {
            // If serialization fails, rollback whole transaction (we're inside @Transactional)
            throw new RuntimeException("Failed to create outbox payload", ex);
        }

        // 6) Return response to caller (publishing deferred)
        if (decision.authorized) {
            return new AuthorizationResponseDTO("AUTHORIZED", decision.authCode, null);
        } else {
            return new AuthorizationResponseDTO("DECLINED", null, decision.reason);
        }
    }
}