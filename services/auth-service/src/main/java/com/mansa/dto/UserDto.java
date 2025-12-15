package com.mansa.dto;


import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String role;
}

