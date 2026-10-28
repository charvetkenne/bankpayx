package com.mansa.infrastructure.operator.cinetpay.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record CinetPayCallbackPayload(
        @JsonProperty("cpm_site_id") String siteId,
        @JsonProperty("cpm_trans_id") String transId,
        @JsonProperty("cpm_trans_date") String transDate,
        @JsonProperty("cpm_amount") String amount,
        @JsonProperty("cpm_currency") String currency,
        @JsonProperty("signature") String signature,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("cel_phone_num") String phoneNumber,
        @JsonProperty("cpm_phone_prefixe") String phonePrefix,
        @JsonProperty("cpm_result") String result,
        @JsonProperty("cpm_error_message") String errorMessage
) {}