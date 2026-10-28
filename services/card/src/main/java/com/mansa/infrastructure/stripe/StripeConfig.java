package com.mansa.infrastructure.stripe;


import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Stripe.
 *
 * Toutes les propriétés sont lues depuis application.yml / variables d'environnement :
 *
 *   stripe:
 *     secret-key:      ${STRIPE_SECRET_KEY}
 *     webhook-secret:  ${STRIPE_WEBHOOK_SECRET}
 *     publishable-key: ${STRIPE_PUBLISHABLE_KEY}
 *     api-version:     2023-10-16
 *     max-retries:     2
 *
 * En production, injecter STRIPE_SECRET_KEY depuis AWS Secrets Manager / Vault.
 * Ne jamais committer la vraie clé dans le code source.
 */




/**
 * Configuration Stripe.
 *
 * Propriétés application.yml :
 *
 *   stripe:
 *     secret-key:      ${STRIPE_SECRET_KEY}
 *     webhook-secret:  ${STRIPE_WEBHOOK_SECRET}
 *     publishable-key: ${STRIPE_PUBLISHABLE_KEY}
 *     return-url:      ${STRIPE_RETURN_URL:http://localhost:5173/payment/callback}
 *     api-version:     2023-10-16
 *     max-retries:     2
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {

    private String secretKey;
    private String webhookSecret;
    private String publishableKey;
    private String apiVersion = "2023-10-16";
    private int    maxNetworkRetries = 2;

    /**
     * URL de retour après qu'une méthode de paiement redirige l'utilisateur
     * (3DS, PayPal, Bancontact, iDEAL, Klarna, etc.).
     *
     * En développement  : http://localhost:5173/payment/callback
     * En production     : https://bankpayx.com/payment/callback
     *
     * Configurer via la variable d'environnement STRIPE_RETURN_URL.
     */
    private String returnUrl = "http://localhost:5173/payment/callback";

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        Stripe.setMaxNetworkRetries(maxNetworkRetries);
        log.info("Stripe SDK initialized – apiVersion={} returnUrl={}", apiVersion, returnUrl);
    }

    /** Construit des RequestOptions avec idempotency key. */
    public RequestOptions requestOptionsFor(String idempotencyKey) {
        return RequestOptions.builder()
                .setApiKey(secretKey)
                //.setStripeVersionOverride(apiVersion)
                .setIdempotencyKey(idempotencyKey)
                .build();
    }
}

 



// @Slf4j
// @Getter
// @Setter
// @Configuration
// @ConfigurationProperties(prefix = "stripe")
// public class StripeConfig {

//     /** Clé secrète Stripe (sk_live_… ou sk_test_…). */
//     private String secretKey;

//     /** Secret de vérification des webhooks Stripe (whsec_…). */
//     private String webhookSecret;

//     /** Clé publique utilisée côté frontend pour Stripe.js. */
//     private String publishableKey;

//     /** Version de l'API Stripe à cibler. */
//     private String apiVersion = "2023-10-16";

//     /**
//      * Nombre de retries automatiques que le SDK Stripe effectuera
//      * en cas d'erreur réseau ou 5xx transitoire.
//      */
//     private int maxNetworkRetries = 2;

//     /**
//      * Initialise le SDK Stripe globalement au démarrage.
//      * Positionne la clé API et le nombre de retries par défaut.
//      */
//     @PostConstruct
//     public void init() {
//         Stripe.apiKey              = secretKey;
//         Stripe.setMaxNetworkRetries(maxNetworkRetries);
//         log.info("Stripe SDK initialized – apiVersion={} maxNetworkRetries={}",
//                 apiVersion, maxNetworkRetries);
//     }

//     /**
//      * Construit des RequestOptions portant la version d'API et une idempotency key.
//      * À utiliser pour les appels qui doivent être idempotents (ex: createPaymentIntent).
//      *
//      * @param idempotencyKey clé unique par tentative (ex: transactionId + "-attempt-1")
//      */
    
//     public RequestOptions buildRequestOptions(String idempotencyKey) {
//         return RequestOptions.builder()
//                 .setApiKey(secretKey)
//                 //.setStripeVersionOverride(apiVersion)
//                 .setIdempotencyKey(idempotencyKey)
//                 .build();
//     }
// }
