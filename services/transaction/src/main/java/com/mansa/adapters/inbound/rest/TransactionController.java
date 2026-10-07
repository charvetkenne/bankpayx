package com.mansa.adapters.inbound.rest;


import com.mansa.application.port.in.CreateTransactionCommandPort;
import com.mansa.application.service.dto.CreateTransactionRequest;
import com.mansa.application.service.dto.TransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final CreateTransactionCommandPort service;

  public TransactionController(CreateTransactionCommandPort service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> create(@Validated @RequestBody CreateTransactionRequest request) {
    TransactionResponse response = service.create(request);
    return ResponseEntity.status(201).body(response);
  }

  // endpoint sync processing
  @PostMapping("/{id}/process")
  public ResponseEntity<TransactionResponse> process(@PathVariable("id") UUID id) {
    // cast to implementation to access process method or expose via port
    if (service instanceof com.mansa.application.service.TransactionApplicationService s) {
      TransactionResponse r = s.process(id);
      return ResponseEntity.ok(r);
    }
    return ResponseEntity.status(501).build();
  }
}
