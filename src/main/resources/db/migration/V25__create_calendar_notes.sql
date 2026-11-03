CREATE TABLE calendar_notes (
 id UUID PRIMARY KEY,
 owner_id UUID NOT NULL REFERENCES users(id),
 note_date DATE NOT NULL,
 content TEXT NOT NULL,
 reminder_at TIMESTAMP WITH TIME ZONE,
 reminder_zone VARCHAR(80),
 reminder_sent_at TIMESTAMP WITH TIME ZONE,
 next_attempt_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_calendar_notes_owner_date ON calendar_notes(owner_id, note_date);
CREATE INDEX idx_calendar_notes_reminders ON calendar_notes(reminder_at) WHERE reminder_sent_at IS NULL;
