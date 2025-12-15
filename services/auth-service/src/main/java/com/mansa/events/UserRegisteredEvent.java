package com.mansa.events;


import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRegisteredEvent {
    private UUID userId;
    private String username;
    private String email;
}
