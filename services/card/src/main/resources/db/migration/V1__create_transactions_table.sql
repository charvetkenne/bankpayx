-- ============================================================
-- card-service | V1 — schéma final de la table transactions
--
-- Pas de données brutes de carte (PAN, CVV, expiry) :
-- la tokenisation est faite par Stripe.js côté frontend.
-- Les informations affichables (last4, brand) sont peuplées
-- depuis la réponse Stripe après autorisation.
-- ============================================================

-- =============================================================================
-- V1__create_transactions_table.sql (Version complète et corrigée)
-- =============================================================================

CREATE TABLE IF NOT EXISTS transactions (
    -- ── Identifiants ─────────────────────────────────────────
    transaction_id              VARCHAR(100)    NOT NULL,
    user_id                     UUID            NOT NULL, -- LIEN SUPABASE AUTH

    -- ── Données carte (nullables — depuis réponse Stripe/PayPal) ───
    masked_card_number          VARCHAR(25),
    card_holder                 VARCHAR(100),
    card_type                   VARCHAR(15),

    -- ── Montant ─────────────────────────────────────────────
    amount                      NUMERIC(19, 2)  NOT NULL,
    currency                    VARCHAR(5)      NOT NULL,

    -- ── Merchant / contexte ──────────────────────────────────
    merchant_id                 VARCHAR(100)    NOT NULL,
    description                 VARCHAR(255),
    correlation_id              VARCHAR(100),

    -- ── Gateway (Stripe ou PayPal) ───────────────────────────
    gateway_payment_method_id   VARCHAR(200),   -- pm_xxx (Stripe) ou order_id (PayPal)
    gateway_transaction_id      VARCHAR(200),   -- pi_xxx (Stripe) ou capture_id (PayPal)
    failure_reason              VARCHAR(500),

    -- ── Statut / timestamps ──────────────────────────────────
    status                      VARCHAR(20)     NOT NULL,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    -- ── Contraintes ──────────────────────────────────────────
    CONSTRAINT pk_transactions PRIMARY KEY (transaction_id),
    CONSTRAINT fk_transactions_user 
        FOREIGN KEY (user_id) 
        REFERENCES auth.users(id) 
        ON DELETE CASCADE
);

-- ── Index pour les performances ──────────────────────────────
CREATE INDEX IF NOT EXISTS idx_tx_user_id             ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_tx_gateway_pm_id       ON transactions(gateway_payment_method_id);
CREATE INDEX IF NOT EXISTS idx_tx_gateway_id          ON transactions(gateway_transaction_id);
CREATE INDEX IF NOT EXISTS idx_tx_merchant_id         ON transactions(merchant_id);
CREATE INDEX IF NOT EXISTS idx_tx_status              ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_tx_correlation_id      ON transactions(correlation_id);



-- CREATE TABLE IF NOT EXISTS transactions (

--     -- ── Identifiant ─────────────────────────────────────────
--     transaction_id              VARCHAR(100)    NOT NULL,

--     -- ── Données carte (nullables — depuis réponse Stripe) ───
--     masked_card_number          VARCHAR(25),
--     card_holder                 VARCHAR(100),
--     card_type                   VARCHAR(15),

--     -- ── Montant ─────────────────────────────────────────────
--     amount                      NUMERIC(19, 2)  NOT NULL,
--     currency                    VARCHAR(5)      NOT NULL,

--     -- ── Merchant / contexte ──────────────────────────────────
--     merchant_id                 VARCHAR(100)    NOT NULL,
--     description                 VARCHAR(255),
--     correlation_id              VARCHAR(100),

--     -- ── Gateway Stripe ───────────────────────────────────────
--     gateway_payment_method_id   VARCHAR(200),   -- pm_xxx  : token Stripe.js
--     gateway_transaction_id      VARCHAR(200),   -- pi_xxx  : PaymentIntent Stripe
--     failure_reason              VARCHAR(500),

--     -- ── Statut / timestamps ──────────────────────────────────
--     status                      VARCHAR(20)     NOT NULL,
--     created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
--     updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

--     CONSTRAINT pk_transactions PRIMARY KEY (transaction_id)
-- );

-- -- ── Index ────────────────────────────────────────────────────
-- CREATE INDEX idx_tx_gateway_pm_id   ON transactions(gateway_payment_method_id);
-- CREATE INDEX idx_tx_gateway_id      ON transactions(gateway_transaction_id);
-- CREATE INDEX idx_tx_merchant_id     ON transactions(merchant_id);
-- CREATE INDEX idx_tx_status          ON transactions(status);
-- CREATE INDEX idx_tx_correlation_id  ON transactions(correlation_id);