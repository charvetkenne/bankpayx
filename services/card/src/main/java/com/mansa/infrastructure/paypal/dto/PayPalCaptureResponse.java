package com.mansa.infrastructure.paypal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PayPalCaptureResponse {

    @JsonProperty("id")             private String             id;
    @JsonProperty("status")         private String             status;
    @JsonProperty("purchase_units") private List<PurchaseUnit> purchaseUnits;

    @Getter @Setter
    public static class PurchaseUnit {
        @JsonProperty("payments") private Payments payments;
    }

    @Getter @Setter
    public static class Payments {
        @JsonProperty("captures") private List<Capture> captures;
    }

    @Getter @Setter
    public static class Capture {
        @JsonProperty("id")     private String id;
        @JsonProperty("status") private String status;
    }

    /** Extrait le premier captureId depuis la réponse. */
    public String extractCaptureId() {
        try {
            return purchaseUnits.get(0).getPayments().getCaptures().get(0).getId();
        } catch (Exception ex) {
            return null;
        }
    }
}
