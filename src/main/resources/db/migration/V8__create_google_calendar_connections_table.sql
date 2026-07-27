CREATE TABLE google_calendar_connections (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    access_token TEXT,
    access_token_expires_at TIMESTAMPTZ,
    refresh_token TEXT,
    authorization_state VARCHAR(160),
    state_expires_at TIMESTAMPTZ,
    calendar_id VARCHAR(255) NOT NULL DEFAULT 'primary',
    connected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_google_calendar_connections_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_google_calendar_connections_state
    ON google_calendar_connections (authorization_state);
