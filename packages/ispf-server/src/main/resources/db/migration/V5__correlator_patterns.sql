-- Multi-event correlator patterns (COUNT | SEQUENCE)

ALTER TABLE event_correlators
    ADD COLUMN IF NOT EXISTS pattern_type VARCHAR(32) NOT NULL DEFAULT 'COUNT';

ALTER TABLE event_correlators
    ADD COLUMN IF NOT EXISTS second_event_name VARCHAR(128);

ALTER TABLE correlator_hits
    ADD COLUMN IF NOT EXISTS event_name VARCHAR(128);
