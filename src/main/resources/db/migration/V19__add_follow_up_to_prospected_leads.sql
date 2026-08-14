ALTER TABLE prospected_leads
    ADD COLUMN follow_up_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN next_follow_up_date DATE,
    ADD COLUMN last_follow_up_at TIMESTAMPTZ;

UPDATE prospected_leads
SET next_follow_up_date = ((prospected_at AT TIME ZONE 'America/Sao_Paulo')::date + 3)
WHERE next_follow_up_date IS NULL;

CREATE INDEX idx_prospected_leads_owner_next_follow_up
    ON prospected_leads (owner_id, next_follow_up_date);
