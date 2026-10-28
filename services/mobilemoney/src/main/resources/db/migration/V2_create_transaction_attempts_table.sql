CREATE TABLE transaction_attempts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id  UUID            NOT NULL REFERENCES mobile_money_transactions(id) ON DELETE CASCADE,
    operator_code   VARCHAR(20)     NOT NULL,
    attempt_number  INT             NOT NULL,
    attempted_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    result_code     VARCHAR(50),
    result_message  TEXT,
    successful      BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_attempt_number_positive CHECK (attempt_number > 0),
    CONSTRAINT uq_transaction_attempt_number UNIQUE (transaction_id, attempt_number)
);

CREATE INDEX idx_ta_transaction_id ON transaction_attempts (transaction_id);
CREATE INDEX idx_ta_attempted_at   ON transaction_attempts (attempted_at DESC);

COMMENT ON TABLE transaction_attempts IS 'Audit log of each attempt made per transaction (for retry tracking)';