CREATE TABLE prospected_leads (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    provider_place_id VARCHAR(255),
    deduplication_key VARCHAR(600) NOT NULL,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(200),
    city VARCHAR(120) NOT NULL,
    state VARCHAR(2) NOT NULL,
    public_address VARCHAR(500),
    website VARCHAR(500),
    phone VARCHAR(50),
    source_url VARCHAR(500),
    priority_score INTEGER,
    opening_message TEXT,
    prospected_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_prospected_leads_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT uq_prospected_leads_owner_key UNIQUE (owner_id, deduplication_key),
    CONSTRAINT ck_prospected_leads_score CHECK (priority_score IS NULL OR priority_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_prospected_leads_owner_prospected_at
    ON prospected_leads (owner_id, prospected_at DESC);
