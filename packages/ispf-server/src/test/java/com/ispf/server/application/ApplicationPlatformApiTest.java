package com.ispf.server.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationPlatformApiTest {

    private static final String DEMO_DEVICE = "root.platform.devices.demo-sensor-01";
    private static final String APP_ID = "terminal-test";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registersApplicationAndMigratesData() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appId": "%s",
                                  "displayName": "Terminal Test",
                                  "tablePrefix": ""
                                }
                                """.formatted(APP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appId").value(APP_ID));

        mockMvc.perform(post("/api/v1/applications/%s/data/migrate".formatted(APP_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": "1.0.0",
                                  "scripts": [
                                    {
                                      "id": "dispatch_order",
                                      "sql": "CREATE TABLE IF NOT EXISTS dispatch_order (id UUID PRIMARY KEY, order_number VARCHAR(64) NOT NULL, status VARCHAR(32) NOT NULL);"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applied", hasItem("dispatch_order")));

        mockMvc.perform(get("/api/v1/applications/%s/data/status".formatted(APP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value("1.0.0"));
    }

    @Test
    void deploysScriptFunctionAndInvokesViaBff() throws Exception {
        String orderId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/applications/%s/deploy".formatted(APP_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": "1.0.1",
                                  "displayName": "Terminal Test",
                                  "migrations": [
                                    {
                                      "id": "dispatch_order",
                                      "sql": "CREATE TABLE IF NOT EXISTS dispatch_order (id UUID PRIMARY KEY, order_number VARCHAR(64) NOT NULL, status VARCHAR(32) NOT NULL); INSERT INTO dispatch_order (id, order_number, status) VALUES ('%s', 'DO-TEST-01', 'ready');"
                                    }
                                  ],
                                  "functions": [
                                    {
                                      "objectPath": "%s",
                                      "functionName": "terminal_ping",
                                      "version": "1",
                                      "descriptor": {
                                        "inputSchema": {
                                          "name": "terminal_ping_input",
                                          "fields": [{"name": "orderId", "type": "STRING"}]
                                        },
                                        "outputSchema": {
                                          "name": "terminal_ping_output",
                                          "fields": [
                                            {"name": "error_code", "type": "STRING"},
                                            {"name": "error_message", "type": "STRING"},
                                            {"name": "status", "type": "STRING"}
                                          ]
                                        }
                                      },
                                      "source": {
                                        "type": "script",
                                        "body": "{\\"steps\\":[{\\"type\\":\\"selectOne\\",\\"var\\":\\"order\\",\\"sql\\":\\"SELECT status FROM dispatch_order WHERE id = ?\\",\\"params\\":[\\"${input.orderId}\\"]},{\\"type\\":\\"failIfNull\\",\\"var\\":\\"order\\",\\"error_code\\":\\"NOT_FOUND\\",\\"error_message\\":\\"missing\\"},{\\"type\\":\\"return\\",\\"fields\\":{\\"error_code\\":\\"OK\\",\\"error_message\\":\\"\\",\\"status\\":\\"${order.status}\\"}}]}"
                                      }
                                    }
                                  ]
                                }
                                """.formatted(orderId, DEMO_DEVICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        mockMvc.perform(post("/api/v1/bff/invoke")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "objectPath": "%s",
                                  "functionName": "terminal_ping",
                                  "input": {
                                    "schema": {
                                      "name": "terminal_ping_input",
                                      "fields": [{"name": "orderId", "type": "STRING"}]
                                    },
                                    "rows": [{"orderId": "%s"}]
                                  }
                                }
                                """.formatted(DEMO_DEVICE, orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_code").value("OK"))
                .andExpect(jsonPath("$.result.status").value("ready"));
    }
}
