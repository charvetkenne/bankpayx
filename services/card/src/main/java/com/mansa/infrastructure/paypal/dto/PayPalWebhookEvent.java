package com.mansa.infrastructure.paypal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PayPalWebhookEvent {

    @JsonProperty("id")          private String id;
    @JsonProperty("event_type")  private String eventType;
    @JsonProperty("resource")    private Object resource;
    @JsonProperty("summary")     private String summary;
}
