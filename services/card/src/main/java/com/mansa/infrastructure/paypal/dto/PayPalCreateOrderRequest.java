package com.mansa.infrastructure.paypal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class PayPalCreateOrderRequest {

    @JsonProperty("intent")
    private String intent;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    @JsonProperty("application_context")
    private ApplicationContext applicationContext;

    @Getter @Builder
    public static class PurchaseUnit {
        @JsonProperty("amount")      private Amount amount;
        @JsonProperty("description") private String description;
    }

    @Getter @Builder
    public static class Amount {
        @JsonProperty("currency_code") private String currencyCode;
        @JsonProperty("value")         private String value;
    }

    @Getter @Builder
    public static class ApplicationContext {
        @JsonProperty("return_url")    private String returnUrl;
        @JsonProperty("cancel_url")    private String cancelUrl;
        @JsonProperty("brand_name")    private String brandName;
        @JsonProperty("user_action")   private String userAction;   // PAY_NOW
        @JsonProperty("landing_page")  private String landingPage;  // LOGIN
    }
}
