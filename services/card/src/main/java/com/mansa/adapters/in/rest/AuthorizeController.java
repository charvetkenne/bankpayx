package com.mansa.adapters.in.rest;

import com.mansa.application.port.in.AuthorizeCardUseCase;
import com.mansa.dto.AuthorizationRequestDTO;
import com.mansa.dto.AuthorizationResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class AuthorizeController {
    private final AuthorizeCardUseCase service;
    public AuthorizeController(AuthorizeCardUseCase service){ this.service = service; }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponseDTO> authorize(@RequestBody @Validated AuthorizationRequestDTO req){
        var resp = service.authorize(req);
        if ("AUTHORIZED".equals(resp.status)) return ResponseEntity.ok(resp);
        return ResponseEntity.status(402).body(resp);
    }
}