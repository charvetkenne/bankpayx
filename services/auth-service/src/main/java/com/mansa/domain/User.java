package com.mansa.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
@Data
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (role == null) role = Role.ROLE_USER;
    }

    public static String  withUsername(String username2) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'withUsername'");
        return username2;
    }
}

