package com.mansa.infrastructure.keycloak;

import com.mansa.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeycloakUserClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.admin.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.token}")
    private String adminToken;

    public UUID createUser(RegisterRequest req) {

        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        Map<String, Object> body = Map.of(
                "username", req.getUsername(),
                "email", req.getEmail(),
                "enabled", true,
                "credentials", new Object[]{
                        Map.of(
                                "type", "password",
                                "value", req.getPassword(),
                                "temporary", false
                        )
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response =
                restTemplate.exchange(url, HttpMethod.POST, request, Void.class);

        String location = response.getHeaders().getLocation().toString();

        return UUID.fromString(location.substring(location.lastIndexOf("/") + 1));
    }
}
