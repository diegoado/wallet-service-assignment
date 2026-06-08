CREATE TABLE wallet (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    document   VARCHAR(14)  NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT now(),

    CONSTRAINT pk_wallet          PRIMARY KEY (id),
    CONSTRAINT uk_wallet_document UNIQUE (document)
);

CREATE TABLE transaction_ledger (
    id               UUID          NOT NULL DEFAULT gen_random_uuid(),
    wallet_id        UUID          NOT NULL,
    currency         VARCHAR(3)    NOT NULL,
    amount           DECIMAL(19,2) NOT NULL,
    type             VARCHAR(20)   NOT NULL,
    correlation_id   UUID,
    idempotency_key  VARCHAR(255)  NOT NULL,
    idempotency_date DATE          NOT NULL,
    description      TEXT,
    created_at       TIMESTAMP(3)  NOT NULL DEFAULT now(),

    CONSTRAINT pk_transaction_ledger PRIMARY KEY (id),
    CONSTRAINT fk_ledger_wallet      FOREIGN KEY (wallet_id) REFERENCES wallet(id),
    CONSTRAINT uk_ledger_idempotency UNIQUE (wallet_id, idempotency_key, idempotency_date)
);

CREATE INDEX idx_ledger_wallet_currency_created
    ON transaction_ledger (wallet_id, currency, created_at);
