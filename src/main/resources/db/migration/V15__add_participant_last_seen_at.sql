ALTER TABLE participants
    ADD COLUMN last_seen_at TIMESTAMPTZ;

UPDATE participants
SET last_seen_at = COALESCE(left_at, joined_at, CURRENT_TIMESTAMP);

ALTER TABLE participants
    ALTER COLUMN last_seen_at SET NOT NULL;

CREATE INDEX idx_participants_active_last_seen
    ON participants (last_seen_at)
    WHERE left_at IS NULL;
