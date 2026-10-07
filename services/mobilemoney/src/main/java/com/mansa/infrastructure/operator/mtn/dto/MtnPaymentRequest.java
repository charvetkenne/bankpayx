package com.mansa.infrastructure.operator.mtn.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MtnPaymentRequest(
        @JsonProperty("amount") String amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("externalId") String externalId,
        @JsonProperty("payer") MtnPayer payer,
        @JsonProperty("payerMessage") String payerMessage,
        @JsonProperty("payeeNote") String payeeNote
) {
    public record MtnPayer(
            @JsonProperty("partyIdType") String partyIdType,
            @JsonProperty("partyId") String partyId
    ) {}
}
