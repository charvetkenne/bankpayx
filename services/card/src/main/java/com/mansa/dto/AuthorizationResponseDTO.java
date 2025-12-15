package com.mansa.dto;

public class AuthorizationResponseDTO {
    public String status; // AUTHORIZED | DECLINED | ERROR
    public String authCode;
    public String message;
    public AuthorizationResponseDTO() {}
    public AuthorizationResponseDTO(String status, String authCode, String message) {
        this.status = status; this.authCode = authCode; this.message = message;
    }
}
