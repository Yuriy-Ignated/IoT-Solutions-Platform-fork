CREATE TABLE applications (
    app_id VARCHAR(64) PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    table_prefix VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE application_data_migrations (
    id UUID PRIMARY KEY,
    app_id VARCHAR(64) NOT NULL REFERENCES applications(app_id),
    version VARCHAR(64) NOT NULL,
    script_id VARCHAR(128) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_app_migration UNIQUE (app_id, version, script_id)
);

CREATE TABLE application_functions (
    id UUID PRIMARY KEY,
    app_id VARCHAR(64) NOT NULL REFERENCES applications(app_id),
    object_path VARCHAR(512) NOT NULL,
    function_name VARCHAR(128) NOT NULL,
    version VARCHAR(64) NOT NULL DEFAULT '1',
    source_type VARCHAR(32) NOT NULL,
    source_body TEXT NOT NULL,
    input_schema_json TEXT NOT NULL,
    output_schema_json TEXT NOT NULL,
    deployed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_app_function UNIQUE (app_id, object_path, function_name, version)
);

CREATE TABLE platform_schedules (
    schedule_id VARCHAR(128) PRIMARY KEY,
    app_id VARCHAR(64) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    interval_ms BIGINT NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    action_json TEXT NOT NULL,
    last_tick_at TIMESTAMP,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE model_definitions (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    definition_json TEXT NOT NULL,
    builtin BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE function_invoke_audit (
    id UUID PRIMARY KEY,
    correlation_id VARCHAR(64),
    object_path VARCHAR(512),
    function_name VARCHAR(128),
    app_id VARCHAR(64),
    success BOOLEAN NOT NULL,
    error_message VARCHAR(1024),
    invoked_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE workflow_cancel_journal (
    id UUID PRIMARY KEY,
    instance_id VARCHAR(64) NOT NULL,
    workflow_path VARCHAR(512) NOT NULL,
    reason VARCHAR(128) NOT NULL,
    detail_json TEXT,
    cancelled_by VARCHAR(128),
    cancelled_at TIMESTAMP NOT NULL DEFAULT NOW()
);
