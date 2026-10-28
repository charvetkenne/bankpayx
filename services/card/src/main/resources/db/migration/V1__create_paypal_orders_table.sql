-- ============================================================
-- card-service | V2 — table paypal_orders
--
-- Table SÉPARÉE de transactions (Stripe).
-- Stripe et PayPal ne partagent pas la même table.
-- ============================================================

CREATE TABLE IF NOT EXISTS paypal_orders (

    -- ── Identifiant ─────────────────────────────────────────
    transaction_id      VARCHAR(100)    NOT NULL,

    -- ── Identifiants PayPal ──────────────────────────────────
    paypal_order_id     VARCHAR(200),           -- ORDER-xxx retourné par PayPal
    paypal_capture_id   VARCHAR(200),           -- CAPTURE-xxx après capture

    -- ── Montant ─────────────────────────────────────────────
    amount              NUMERIC(19, 2)  NOT NULL,
    currency            VARCHAR(5)      NOT NULL,

    -- ── Merchant / contexte ──────────────────────────────────
    merchant_id         VARCHAR(100)    NOT NULL,
    description         VARCHAR(255),
    correlation_id      VARCHAR(100),

    -- ── URLs PayPal ──────────────────────────────────────────
    return_url          VARCHAR(500),
    cancel_url          VARCHAR(500),
    approve_url         VARCHAR(1000),           -- URL sandbox.paypal.com

    -- ── Statut ───────────────────────────────────────────────
    failure_reason      VARCHAR(500),
    status              VARCHAR(30)     NOT NULL,

    -- ── Timestamps ───────────────────────────────────────────
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_paypal_orders PRIMARY KEY (transaction_id)
);

CREATE INDEX IF NOT EXISTS idx_pp_order_id    ON paypal_orders(paypal_order_id);
CREATE INDEX IF NOT EXISTS idx_pp_capture_id  ON paypal_orders(paypal_capture_id);
CREATE INDEX IF NOT EXISTS idx_pp_merchant_id ON paypal_orders(merchant_id);
CREATE INDEX IF NOT EXISTS idx_pp_status      ON paypal_orders(status);
CREATE INDEX IF NOT EXISTS idx_pp_correlation ON paypal_orders(correlation_id);
