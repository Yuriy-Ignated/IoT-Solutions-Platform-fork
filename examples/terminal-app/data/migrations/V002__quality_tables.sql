CREATE TABLE IF NOT EXISTS tank_balance (
    tank_path VARCHAR(255) PRIMARY KEY,
    product_grade VARCHAR(64) NOT NULL,
    book_liters NUMERIC(14, 3) NOT NULL,
    measured_liters NUMERIC(14, 3) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sample (
    id UUID PRIMARY KEY,
    tank_path VARCHAR(255) NOT NULL,
    taken_at TIMESTAMP NOT NULL,
    quality_ok BOOLEAN NOT NULL,
    certificate_ref VARCHAR(128) NOT NULL
);
