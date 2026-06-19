package com.ispf.server.application.script;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ispf.core.model.DataRecord;
import com.ispf.core.model.DataSchema;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Executes JSON step scripts for application-deployed functions (REQ-PF-01).
 */
@Component
public class FunctionScriptEngine {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public FunctionScriptEngine(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public DataRecord execute(String sourceBody, DataRecord input, DataSchema outputSchema) {
        try {
            JsonNode root = objectMapper.readTree(sourceBody);
            JsonNode steps = root.get("steps");
            if (steps == null || !steps.isArray()) {
                throw new IllegalArgumentException("Script must contain steps array");
            }

            Map<String, Object> inputMap = input != null && input.rowCount() > 0
                    ? new LinkedHashMap<>(input.firstRow())
                    : new LinkedHashMap<>();
            Map<String, Object> vars = new LinkedHashMap<>();
            vars.put("input", inputMap);

            for (JsonNode step : steps) {
                String type = step.path("type").asText();
                switch (type) {
                    case "selectOne" -> {
                        String var = step.path("var").asText();
                        String sql = step.path("sql").asText();
                        List<Object> params = resolveParams(step.get("params"), vars);
                        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
                        vars.put(var, rows.isEmpty() ? null : rows.get(0));
                    }
                    case "exec" -> {
                        String sql = step.path("sql").asText();
                        List<Object> params = resolveParams(step.get("params"), vars);
                        jdbcTemplate.update(sql, params.toArray());
                    }
                    case "failIfNull" -> {
                        String var = step.path("var").asText();
                        if (resolvePath(vars, var) == null) {
                            return wireError(outputSchema, step);
                        }
                    }
                    case "failIfNotEquals" -> {
                        Object actual = resolvePath(vars, step.path("var").asText());
                        String expected = step.path("equals").asText();
                        if (!Objects.equals(String.valueOf(actual), expected)) {
                            return wireError(outputSchema, step);
                        }
                    }
                    case "return" -> {
                        Map<String, Object> row = resolveFields(step.get("fields"), vars);
                        return DataRecord.single(outputSchema, row);
                    }
                    default -> throw new IllegalArgumentException("Unknown script step type: " + type);
                }
            }
            throw new IllegalStateException("Script must end with return step");
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Script execution failed: " + ex.getMessage(), ex);
        }
    }

    private static DataRecord wireError(DataSchema outputSchema, JsonNode step) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("error_code", step.path("error_code").asText(step.path("code").asText("ERROR")));
        row.put("error_message", step.path("error_message").asText(step.path("message").asText("Script validation failed")));
        for (var field : outputSchema.fields()) {
            if (row.containsKey(field.name())) {
                continue;
            }
            row.put(field.name(), switch (field.type()) {
                case BOOLEAN -> false;
                case INTEGER, LONG -> 0L;
                case DOUBLE -> 0.0;
                default -> "";
            });
        }
        return DataRecord.single(outputSchema, row);
    }

    private Map<String, Object> resolveFields(JsonNode fieldsNode, Map<String, Object> vars) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (fieldsNode == null || !fieldsNode.isObject()) {
            return row;
        }
        fieldsNode.fields().forEachRemaining(entry -> {
            Object value = resolveValue(entry.getValue().asText(), vars);
            row.put(entry.getKey(), value);
        });
        return row;
    }

    private List<Object> resolveParams(JsonNode paramsNode, Map<String, Object> vars) {
        List<Object> params = new ArrayList<>();
        if (paramsNode == null || !paramsNode.isArray()) {
            return params;
        }
        for (JsonNode node : paramsNode) {
            params.add(resolveValue(node.asText(), vars));
        }
        return params;
    }

    private Object resolveValue(String token, Map<String, Object> vars) {
        if (token == null) {
            return null;
        }
        if (token.startsWith("${") && token.endsWith("}")) {
            return resolvePath(vars, token.substring(2, token.length() - 1));
        }
        if (token.startsWith("$")) {
            return resolvePath(vars, token.substring(1));
        }
        return token;
    }

    @SuppressWarnings("unchecked")
    private Object resolvePath(Map<String, Object> vars, String path) {
        String[] parts = path.split("\\.");
        Object current = vars;
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
