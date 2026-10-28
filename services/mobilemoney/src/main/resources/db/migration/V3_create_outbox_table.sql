CREATE TABLE outbox_messages (
    id              UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_id    VARCHAR(100)    NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    topic           VARCHAR(200)    NOT NULL,
    payload         JSONB           NOT NULL,
    correlation_id  VARCHAR(100)    NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ,
    failed_at       TIMESTAMPTZ,
    error_message   TEXT,
    retry_count     INT             NOT NULL DEFAULT 0,

    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_outbox_status      ON outbox_messages (status) WHERE status = 'PENDING';
CREATE INDEX idx_outbox_created_at  ON outbox_messages (created_at ASC) WHERE status = 'PENDING';
CREATE INDEX idx_outbox_aggregate   ON outbox_messages (aggregate_id);

COMMENT ON TABLE outbox_messages IS 'Transactional Outbox for reliable domain event publishing to Kafka';
COMMENT ON COLUMN outbox_messages.payload IS 'JSON-serialized domain event payload';
COMMENT ON COLUMN outbox_messages.status IS 'PENDING=waiting to publish, PUBLISHED=sent to Kafka, FAILED=max retries exceeded';