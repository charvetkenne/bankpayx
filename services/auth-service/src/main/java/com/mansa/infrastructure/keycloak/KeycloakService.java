package com.mansa.infrastructure.keycloak;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String serverUrl = "http://localhost:8080";
    private final String realm = "bankpayx";

    public void createUser(String username, String email, String password) {

        String url = serverUrl + "/admin/realms/" + realm + "/users";

        Map<String, Object> body = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "credentials", new Object[]{
                        Map.of(
                                "type", "password",
                                "value", password,
                                "temporary", false
                        )
                }
        );

        restTemplate.postForEntity(url, body, Void.class);
    }
}
