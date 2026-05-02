package com.mansa.service;

  

import com.mansa.domain.event.UserRegisteredEvent;
import com.mansa.dto.AuthResponse;
import com.mansa.dto.RegisterRequest;
import com.mansa.infrastructure.kafaka.AuthEventProducer;
import com.mansa.infrastructure.keycloak.KeycloakUserClient;
import lombok.RequiredArgsConstructor;
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

        // 1. Création dans Keycloak
        var userId = keycloakUserClient.createUser(request);

        // 2. Event (source = auth-service ou keycloak sync)
        eventProducer.send("no type yet",
                 UserRegisteredEvent.of(
                        userId,
                        request.getUsername(),
                        request.getEmail()
                )
        );

        return AuthResponse.builder()
                .message("User created in Keycloak")
                .build();
    }
}

