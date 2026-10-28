package com.mansa.infrastructure.client;

import com.mansa.domain.model.CardPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for the Fraud Detection microservice.
 * Returns a risk score (0.0 = safe, 1.0 = high risk).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FraudDetectionClient {

    private final RestTemplate restTemplate;

    @Value("${clients.fraud-detection.url:http://fraud-service}")
    private String fraudServiceUrl;

    public double getRiskScore(CardPayment payment) {
        try {
            String url = fraudServiceUrl + "/api/v1/risk?transactionId=" + payment.getTransactionId();
            Double score = restTemplate.getForObject(url, Double.class);
            log.debug("Fraud risk score for {}: {}", payment.getTransactionId(), score);
            return score != null ? score : 0.0;
        } catch (Exception ex) {
            log.warn("Fraud detection unavailable, defaulting to low risk: {}", ex.getMessage());
            return 0.0;  // Fail open – don't block payments if fraud service is down
        }
    }
}
