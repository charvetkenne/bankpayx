package com.mansa.domain.enums;

public enum PayPalOrderStatus {
    PENDING_APPROVAL,   // Order créé, en attente d'approbation PayPal
    APPROVED,           // Utilisateur a approuvé sur PayPal, en attente de capture
    CAPTURED,           // Paiement capturé avec succès
    FAILED,             // Erreur lors de la création ou capture
    CANCELLED           // Utilisateur a annulé sur PayPal
}
