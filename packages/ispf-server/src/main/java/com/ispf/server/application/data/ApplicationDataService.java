package com.ispf.server.application.data;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationDataService {

    private final ApplicationDataStore store;
    private final JdbcTemplate jdbcTemplate;

    public ApplicationDataService(ApplicationDataStore store, JdbcTemplate jdbcTemplate) {
        this.store = store;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> register(String appId, String displayName, String tablePrefix) {
        store.registerApp(appId, displayName, tablePrefix);
        return Map.of(
                "appId", appId,
                "displayName", displayName,
                "tablePrefix", tablePrefix != null ? tablePrefix : ""
        );
    }

    @Transactional
    public Map<String, Object> migrate(String appId, String version, List<MigrationScript> scripts) {
        ensureApp(appId);
        List<String> applied = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (MigrationScript script : scripts) {
            if (store.isMigrationApplied(appId, version, script.id())) {
                skipped.add(script.id());
                continue;
            }
            for (String statement : splitStatements(script.sql())) {
                if (!statement.isBlank()) {
                    jdbcTemplate.execute(statement);
                }
            }
            store.recordMigration(appId, version, script.id(), script.sql());
            applied.add(script.id());
        }

        return Map.of(
                "appId", appId,
                "version", version,
                "applied", applied,
                "skipped", skipped
        );
    }

    public Map<String, Object> status(String appId) {
        ensureApp(appId);
        List<Map<String, Object>> migrations = store.listMigrations(appId);
        String currentVersion = migrations.isEmpty()
                ? ""
                : String.valueOf(migrations.get(migrations.size() - 1).get("version"));
        return Map.of(
                "appId", appId,
                "currentVersion", currentVersion,
                "applied", migrations
        );
    }

    private void ensureApp(String appId) {
        store.findApp(appId).orElseThrow(() -> new IllegalArgumentException("Unknown application: " + appId));
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.endsWith(";")) {
                current.append(line, 0, line.lastIndexOf(';')).append('\n');
                statements.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(line).append('\n');
            }
        }
        if (current.length() > 0) {
            statements.add(current.toString().trim());
        }
        return statements;
    }

    public record MigrationScript(String id, String sql) {
    }
}
