package com.mansa.service;



import com.mansa.dto.AuthResponse;
import com.mansa.dto.LoginRequest;
import com.mansa.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}

