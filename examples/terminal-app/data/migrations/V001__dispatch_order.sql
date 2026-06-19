CREATE TABLE IF NOT EXISTS dispatch_order (
    id UUID PRIMARY KEY,
    order_number VARCHAR(64) NOT NULL,
    vehicle_plate VARCHAR(32) NOT NULL,
    planned_liters NUMERIC(14, 3) NOT NULL,
    actual_liters NUMERIC(14, 3) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    tank_path VARCHAR(255) NOT NULL,
    rack_id VARCHAR(64) NOT NULL,
    shift_id UUID NOT NULL,
    quality_ok BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dispatch_order_status ON dispatch_order(status);
