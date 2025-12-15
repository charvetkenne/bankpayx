package com.mansa.service;


import com.mansa.config.JwtProvider;
import com.mansa.domain.RefreshToken;
import com.mansa.domain.Role;
import com.mansa.domain.User;
import com.mansa.dto.AuthResponse;
import com.mansa.dto.LoginRequest;
import com.mansa.dto.RegisterRequest;
import com.mansa.events.UserRegisteredEvent;
import com.mansa.repository.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtProvider jwtProvider,
                           KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("username.exists");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("email.exists");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        user = userRepository.save(user);
        String accessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = createAndStoreRefreshToken(user.getId());
        kafkaTemplate.send("user.registered", new UserRegisteredEvent(user.getId(), user.getUsername(), user.getEmail()));
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresInSeconds(jwtProvider.getValiditySeconds())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("invalid.credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid.credentials");
        }
        String accessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = createAndStoreRefreshToken(user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresInSeconds(jwtProvider.getValiditySeconds())
                .build();
    }

    private String createAndStoreRefreshToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        var rt = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiresAt(Instant.now().plusSeconds(60L * 60L * 24L * 30L)) // 30 days
                .build();
        refreshTokenRepository.save(rt);
        return token;
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        var rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("invalid.refresh.token"));
        if (rt.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("refresh.expired");
        
        Objects.requireNonNull( rt,"need not null value");
        var user = userRepository.findById(rt.getUserId()).orElseThrow();
        String accessToken = jwtProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        refreshTokenRepository.delete(rt);
        String newRefresh = createAndStoreRefreshToken(user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefresh)
                .expiresInSeconds(jwtProvider.getValiditySeconds())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> refreshTokenRepository.delete(rt));
    }
}

