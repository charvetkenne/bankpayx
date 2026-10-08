package com.mansa.infrastructure.paypal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PayPalTokenResponse {
    @JsonProperty("access_token") private String  accessToken;
    @JsonProperty("token_type")   private String  tokenType;
    @JsonProperty("expires_in")   private long    expiresIn;
    @JsonProperty("scope")        private String  scope;
}
