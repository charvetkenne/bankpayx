package com.mansa.infrastructure.kafaka;


import com.mansa.domain.ProcessedEvent;
import com.mansa.domain.Role;
import com.mansa.domain.User;
import com.mansa.repository.ProcessedEventRepository;
import com.mansa.repository.UserRepository;
import com.mansa.domain.event.UserCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final ProcessedEventRepository eventRepository;

    @SuppressWarnings("null")
    @KafkaListener(topics = "auth.user.created.v1", groupId = "auth-service")
    @Transactional
    public void onUserCreated(UserCreatedEvent event) {

        try {
            if (eventRepository.existsById(event.eventId())) {
                log.info("Event already processed: {}", event.eventId());
                return;
            }

            if (userRepository.existsById(event.userId())) {
                return;
            }

            User user = User.builder()
                    .id(event.userId())
                    .username(event.username())
                    .email(event.email())
                    .role(Role.ROLE_USER)
                    .build();

            userRepository.save(user);

            eventRepository.save(
                    ProcessedEvent.builder()
                            .eventId(event.eventId())
                            .build()
            );

        } catch (Exception e) {
            log.error("Error processing event {}", event, e);
            throw new RuntimeException(e);
        }
    }
}