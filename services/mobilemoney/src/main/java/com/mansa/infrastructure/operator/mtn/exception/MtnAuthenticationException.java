package com.mansa.infrastructure.operator.mtn.exception;

/**
 * Exception technique levée lorsque l'authentification OAuth2 auprès de MTN MoMo
 * échoue définitivement (identifiants invalides, ou service indisponible après
 * épuisement des tentatives de retry / circuit breaker ouvert).
 */
public class MtnAuthenticationException extends RuntimeException {

    public MtnAuthenticationException(String message) {
        super(message);
    }

    public MtnAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}