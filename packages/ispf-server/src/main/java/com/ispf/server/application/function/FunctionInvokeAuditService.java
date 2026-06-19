package com.ispf.server.application.function;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class FunctionInvokeAuditService {

    private final JdbcTemplate jdbcTemplate;

    public FunctionInvokeAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(String appId, String objectPath, String functionName, boolean success, String errorMessage) {
        jdbcTemplate.update("""
                INSERT INTO function_invoke_audit (
                    id, correlation_id, object_path, function_name, app_id, success, error_message, invoked_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                objectPath,
                functionName,
                appId,
                success,
                errorMessage,
                Timestamp.from(Instant.now())
        );
    }
}
