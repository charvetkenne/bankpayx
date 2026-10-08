package com.mansa.application.port.in;
import com.mansa.dto.AuthorizationRequestDTO;
import com.mansa.dto.AuthorizationResponseDTO;

public interface AuthorizeCardUseCase {
    AuthorizationResponseDTO authorize(AuthorizationRequestDTO request);
}