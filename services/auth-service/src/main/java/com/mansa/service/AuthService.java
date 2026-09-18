package com.mansa.service;


import com.mansa.dto.AuthResponse;
import com.mansa.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
}
