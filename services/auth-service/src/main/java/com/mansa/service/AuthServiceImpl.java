package com.mansa.service;

import com.mansa.domain.event.UserRegisteredEvent;
import com.mansa.dto.AuthResponse;
import com.mansa.dto.RegisterRequest;
import com.mansa.infrastructure.kafaka.AuthEventProducer;
import com.mansa.infrastructure.keycloak.KeycloakUserClient;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final KeycloakUserClient keycloakUserClient;
    private final AuthEventProducer eventProducer;

    @Override
    public AuthResponse register(RegisterRequest request) {

        // 1. Création utilisateur dans Keycloak
        UUID userId = keycloakUserClient.createUser(request);

        // 2. Event métier (optionnel si SPI déjà actif)
        UserRegisteredEvent event = UserRegisteredEvent.of(
                userId,
                request.getUsername(),
                request.getEmail()
        );

        eventProducer.send( event);

        return AuthResponse.builder()
                .message("User successfully created in Keycloak")
                .build();
    }
}