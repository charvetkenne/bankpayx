package com.mansa.infrastructure.security;


import com.mansa.domain.execption.HmacValidationException;
import com.mansa.domain.valueobject.OperatorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Component
public class HmacSignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final Map<String, String> operatorSecrets;

    public HmacSignatureValidator(
            @Value("${operators.mtn.api-secret}") String mtnSecret,
            @Value("${operators.orange.client-secret}") String orangeSecret,
            @Value("${operators.wave.webhook-secret}") String waveSecret,
            @Value("${operators.cinetpay.secret-key}") String cinetPaySecret,
            @Value("${operators.notchpay.webhook-hash}")      String notchPayHash
    ) {
        this.operatorSecrets = Map.of(
                OperatorCode.MTN.name(), mtnSecret,
                OperatorCode.ORANGE.name(), orangeSecret,
                OperatorCode.WAVE.name(), waveSecret,
                OperatorCode.CINETPAY.name(), cinetPaySecret,
                OperatorCode.NOTCHPAY.name(), notchPayHash
        );
    }

    /**
     * Validates the HMAC-SHA256 signature of a callback payload.
     *
     * @param operatorCode  the operator sending the callback
     * @param rawPayload    the raw request body as received
     * @param receivedHmac  the HMAC signature from the request header
     * @throws HmacValidationException if validation fails
     */
    public void validate(String operatorCode, String rawPayload, String receivedHmac) {
        String secret = operatorSecrets.get(operatorCode.toUpperCase());
        if (secret == null) {
            throw new HmacValidationException(operatorCode, "No secret configured for operator");
        }

        String computedHmac = computeHmac(rawPayload, secret);

        if (!timingSafeEquals(computedHmac, receivedHmac)) {
            log.warn("HMAC validation failed: operator={}, expected={}, received={}",
                    operatorCode, computedHmac, receivedHmac);
            throw new HmacValidationException(operatorCode, "Signature mismatch");
        }

        log.debug("HMAC validation passed: operator={}", operatorCode);
    }

    public String computeHmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256
            );
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }

    /**
     * Timing-safe string comparison to prevent timing attacks.
     */
    private boolean timingSafeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) return false;

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
