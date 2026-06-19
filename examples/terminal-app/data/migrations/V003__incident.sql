CREATE TABLE IF NOT EXISTS incident (
    id UUID PRIMARY KEY,
    incident_code VARCHAR(32) NOT NULL,
    incident_type VARCHAR(64) NOT NULL,
    tank_path VARCHAR(255),
    rack_id VARCHAR(64),
    order_id UUID,
    comment_text VARCHAR(1024) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_incident_status ON incident(status);
