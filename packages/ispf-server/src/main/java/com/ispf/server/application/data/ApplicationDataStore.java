package com.ispf.server.application.data;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ApplicationDataStore {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationDataStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registerApp(String appId, String displayName, String tablePrefix) {
        if (findApp(appId).isPresent()) {
            jdbcTemplate.update("""
                    UPDATE applications
                    SET display_name = ?, table_prefix = ?
                    WHERE app_id = ?
                    """,
                    displayName,
                    tablePrefix,
                    appId
            );
            return;
        }
        jdbcTemplate.update("""
                INSERT INTO applications (app_id, display_name, table_prefix, created_at)
                VALUES (?, ?, ?, ?)
                """,
                appId,
                displayName,
                tablePrefix,
                Timestamp.from(Instant.now())
        );
    }

    public Optional<Map<String, Object>> findApp(String appId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT app_id, display_name, table_prefix, created_at FROM applications WHERE app_id = ?",
                appId
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public boolean isMigrationApplied(String appId, String version, String scriptId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM application_data_migrations
                WHERE app_id = ? AND version = ? AND script_id = ?
                """,
                Integer.class,
                appId,
                version,
                scriptId
        );
        return count != null && count > 0;
    }

    public void recordMigration(String appId, String version, String scriptId, String sql) {
        jdbcTemplate.update("""
                INSERT INTO application_data_migrations (id, app_id, version, script_id, checksum, applied_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                appId,
                version,
                scriptId,
                checksum(sql),
                Timestamp.from(Instant.now())
        );
    }

    public List<Map<String, Object>> listMigrations(String appId) {
        return jdbcTemplate.queryForList("""
                SELECT version, script_id, checksum, applied_at
                FROM application_data_migrations
                WHERE app_id = ?
                ORDER BY applied_at
                """,
                appId
        );
    }

    private static String checksum(String sql) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sql.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Checksum failed", ex);
        }
    }
}
