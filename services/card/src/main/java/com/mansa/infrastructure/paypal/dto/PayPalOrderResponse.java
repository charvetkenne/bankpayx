package com.mansa.infrastructure.paypal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PayPalOrderResponse {

    @JsonProperty("id")     private String     id;
    @JsonProperty("status") private String     status;
    @JsonProperty("links")  private List<Link> links;

    @Getter @Setter
    public static class Link {
        @JsonProperty("href")   private String href;
        @JsonProperty("rel")    private String rel;
        @JsonProperty("method") private String method;
    }

    /** Extrait l'URL d'approbation depuis les links. */
    public String getApproveUrl() {
        if (links == null) return null;
        return links.stream()
                .filter(l -> "approve".equals(l.getRel()))
                .map(Link::getHref)
                .findFirst()
                .orElse(null);
    }
}
