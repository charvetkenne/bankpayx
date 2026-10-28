CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE mobile_money_transactions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    idempotency_key     VARCHAR(128)    NOT NULL UNIQUE,
    phone_number        VARCHAR(20)     NOT NULL,
    amount              NUMERIC(19, 4)  NOT NULL,
    currency            VARCHAR(3)      NOT NULL,
    operator_code       VARCHAR(20)     NOT NULL,
    status              VARCHAR(20)     NOT NULL,
    operator_reference  VARCHAR(255),
    customer_id         VARCHAR(100)    NOT NULL,
    correlation_id      VARCHAR(100)    NOT NULL,
    failure_reason      TEXT,
    failure_code        VARCHAR(50),
    cancellation_reason TEXT,
    retry_count         INT             NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMPTZ,

    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_retry_count     CHECK (retry_count >= 0),
    CONSTRAINT chk_status          CHECK (status IN (
        'INITIATED', 'PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'EXPIRED'
    ))
);

CREATE INDEX idx_mmt_idempotency_key  ON mobile_money_transactions (idempotency_key);
CREATE INDEX idx_mmt_status           ON mobile_money_transactions (status);
CREATE INDEX idx_mmt_customer_id      ON mobile_money_transactions (customer_id);
CREATE INDEX idx_mmt_correlation_id   ON mobile_money_transactions (correlation_id);
CREATE INDEX idx_mmt_created_at       ON mobile_money_transactions (created_at DESC);
CREATE INDEX idx_mmt_operator_status  ON mobile_money_transactions (operator_code, status);

COMMENT ON TABLE mobile_money_transactions IS 'Core table for BankPayX Mobile Money transactions';
COMMENT ON COLUMN mobile_money_transactions.idempotency_key IS 'Client-provided idempotency key to prevent duplicate payments';
COMMENT ON COLUMN mobile_money_transactions.operator_reference IS 'Reference returned by the mobile money operator';