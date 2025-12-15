package com.mansa.domain.aggregate;
import com.mansa.domain.entity.CardTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CardAuthorization {
    private final CardTransaction tx;

    public CardAuthorization(String pan, BigDecimal amount, String currency, String merchantId, String rrn) {
        this.tx = new CardTransaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setPanMasked(maskPan(pan));
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setMerchantId(merchantId);
        tx.setRrn(rrn);
        tx.setRequestTime(Instant.now());
    }

    public CardTransaction getTransaction() { return tx; }

    public Decision authorize(String expiry) {
        if (expiry == null || expiry.length() != 4) {
            tx.setStatus("DECLINED");
            tx.setResponseTime(Instant.now());
            return Decision.decline("Invalid expiry");
        }
        // Example rule: limit per transaction
        if (tx.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            tx.setStatus("DECLINED");
            tx.setResponseTime(Instant.now());
            return Decision.decline("Amount exceeds single-transaction limit");
        }
        // BIN check example (simplified)
        if (tx.getPanMasked().startsWith("000000")) {
            tx.setStatus("DECLINED");
            tx.setResponseTime(Instant.now());
            return Decision.decline("Blocked BIN");
        }
        String authCode = generateAuthCode();
        tx.setAuthCode(authCode);
        tx.setStatus("AUTHORIZED");
        tx.setResponseTime(Instant.now());
        return Decision.authorize(authCode);
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 10) return pan;
        int len = pan.length();
        return pan.substring(0,6) + "******" + pan.substring(len-4);
    }

    private String generateAuthCode(){
        return String.valueOf(Math.abs((tx.getPanMasked().hashCode() + System.currentTimeMillis()) % 1000000));
    }

    public static class Decision {
        public final boolean authorized;
        public final String authCode;
        public final String reason;
        private Decision(boolean a, String authCode, String reason) { this.authorized = a; this.authCode = authCode; this.reason = reason; }
        public static Decision authorize(String authCode) { return new Decision(true, authCode, null); }
        public static Decision decline(String reason){ return new Decision(false, null, reason); }
    }
}