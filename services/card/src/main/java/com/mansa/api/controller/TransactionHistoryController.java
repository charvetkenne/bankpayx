package com.mansa.api.controller;

import com.mansa.api.dto.response.*;
import com.mansa.application.port.in.GetCardTransactionHistoryUseCase;
import com.mansa.application.port.in.GetPayPalOrderHistoryUseCase;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.model.CardPayment;
import com.mansa.domain.model.PayPalOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Tag(name = "Transaction History", description = "Historique des transactions Stripe et PayPal")
public class TransactionHistoryController {

    private final GetCardTransactionHistoryUseCase cardHistoryUseCase;
    private final GetPayPalOrderHistoryUseCase     paypalHistoryUseCase;

    // ── GET /api/v1/history ────────────────────────────────────────────────────
    @GetMapping
    @Operation(
        summary     = "Historique unifié Stripe + PayPal",
        description = "Retourne toutes les transactions (carte et PayPal) triées par date décroissante."
    )
    public ResponseEntity<PagedResponse<UnifiedTransactionResponse>> getUnifiedHistory(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(required = false)     String  merchantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
                System.out.println("HISTORY CONTROLLER CALLED");

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Récupérer les deux types de transactions
         System.out.println("STEP A");
        Page<CardPayment> cardPage = cardHistoryUseCase.getHistory(
                new GetCardTransactionHistoryUseCase.Filter(merchantId, null, from, to), pageable);
         System.out.println("STEP B");
        Page<PayPalOrder> paypalPage = paypalHistoryUseCase.getHistory(
                new GetPayPalOrderHistoryUseCase.Filter(merchantId, null, from, to), pageable);

        // Fusionner et trier par date
        List<UnifiedTransactionResponse> merged = new ArrayList<>();
        cardPage.getContent().stream().map(this::toUnified).forEach(merged::add);
        paypalPage.getContent().stream().map(this::toUnified).forEach(merged::add);
        merged.sort(Comparator.comparing(UnifiedTransactionResponse::getCreatedAt).reversed());

        // Construire la réponse paginée
        long total = cardPage.getTotalElements() + paypalPage.getTotalElements();
        int  pages = (int) Math.ceil((double) total / size);

        return ResponseEntity.ok(PagedResponse.<UnifiedTransactionResponse>builder()
                .content(merged)
                .currentPage(page)
                .pageSize(size)
                .totalElements(total)
                .totalPages(pages)
                .hasNext(page < pages - 1)
                .hasPrevious(page > 0)
                .build());
    }

    // ── GET /api/v1/history/stripe ────────────────────────────────────────────
    @GetMapping("/stripe")
    @Operation(summary = "Historique des transactions Stripe uniquement")
    public ResponseEntity<PagedResponse<TransactionSummaryResponse>> getCardHistory(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String        merchantId,
            @RequestParam(required = false)    PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TransactionSummaryResponse> result = cardHistoryUseCase
                .getHistory(new GetCardTransactionHistoryUseCase.Filter(merchantId, status, from, to), pageable)
                .map(this::toCardSummary);

        return ResponseEntity.ok(PagedResponse.from(result));
    }

    // ── GET /api/v1/history/paypal ────────────────────────────────────────────
    @GetMapping("/paypal")
    @Operation(summary = "Historique des orders PayPal uniquement")
    public ResponseEntity<PagedResponse<PayPalOrderSummaryResponse>> getPayPalHistory(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String            merchantId,
            @RequestParam(required = false)    PayPalOrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<PayPalOrderSummaryResponse> result = paypalHistoryUseCase
                .getHistory(new GetPayPalOrderHistoryUseCase.Filter(merchantId, status, from, to), pageable)
                .map(this::toPayPalSummary);

        return ResponseEntity.ok(PagedResponse.from(result));
    }

    // ── Mappers privés ────────────────────────────────────────────────────────
    private TransactionSummaryResponse toCardSummary(CardPayment p) {
        return TransactionSummaryResponse.builder()
                .transactionId(p.getTransactionId())
                .maskedCardNumber(p.getMaskedCardNumber() != null ? p.getMaskedCardNumber() : "****")
                .cardType(p.getCardType() != null ? p.getCardType().name() : "UNKNOWN")
                .amount(p.getAmount().getAmount())
                .currency(p.getAmount().getCurrency().name())
                .status(p.getStatus().name())
                .merchantId(p.getMerchantId())
                .description(p.getDescription())
                .gatewayTransactionId(p.getGatewayTransactionId())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PayPalOrderSummaryResponse toPayPalSummary(PayPalOrder o) {
        return PayPalOrderSummaryResponse.builder()
                .transactionId(o.getTransactionId())
                .paypalOrderId(o.getPaypalOrderId())
                .captureId(o.getCaptureId())
                .amount(o.getAmount().getAmount())
                .currency(o.getAmount().getCurrency().name())
                .status(o.getStatus().name())
                .merchantId(o.getMerchantId())
                .description(o.getDescription())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private UnifiedTransactionResponse toUnified(CardPayment p) {
        return UnifiedTransactionResponse.builder()
                .transactionId(p.getTransactionId())
                .paymentProvider("STRIPE")
                .amount(p.getAmount().getAmount())
                .currency(p.getAmount().getCurrency().name())
                .status(p.getStatus().name())
                .merchantId(p.getMerchantId())
                .description(p.getDescription())
                .maskedCardNumber(p.getMaskedCardNumber())
                .cardType(p.getCardType() != null ? p.getCardType().name() : null)
                .gatewayTransactionId(p.getGatewayTransactionId())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private UnifiedTransactionResponse toUnified(PayPalOrder o) {
        return UnifiedTransactionResponse.builder()
                .transactionId(o.getTransactionId())
                .paymentProvider("PAYPAL")
                .amount(o.getAmount().getAmount())
                .currency(o.getAmount().getCurrency().name())
                .status(o.getStatus().name())
                .merchantId(o.getMerchantId())
                .description(o.getDescription())
                .paypalOrderId(o.getPaypalOrderId())
                .captureId(o.getCaptureId())
                .approveUrl(o.getApproveUrl())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
