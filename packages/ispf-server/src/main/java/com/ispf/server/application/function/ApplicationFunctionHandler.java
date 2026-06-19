package com.ispf.server.application.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ispf.core.model.DataRecord;
import com.ispf.core.model.DataSchema;
import com.ispf.core.object.FunctionDescriptor;
import com.ispf.server.application.script.FunctionScriptEngine;
import com.ispf.server.function.FunctionHandler;
import com.ispf.server.object.ObjectManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Order(0)
public class ApplicationFunctionHandler implements FunctionHandler {

    private final ApplicationFunctionStore store;
    private final FunctionScriptEngine scriptEngine;
    private final ObjectManager objectManager;
    private final ObjectMapper objectMapper;
    private final FunctionInvokeAuditService auditService;

    public ApplicationFunctionHandler(
            ApplicationFunctionStore store,
            FunctionScriptEngine scriptEngine,
            ObjectManager objectManager,
            ObjectMapper objectMapper,
            FunctionInvokeAuditService auditService
    ) {
        this.store = store;
        this.scriptEngine = scriptEngine;
        this.objectManager = objectManager;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Override
    public boolean supports(String objectPath, String functionName) {
        return store.findLatest(objectPath, functionName).isPresent();
    }

    @Override
    public DataRecord invoke(String objectPath, String functionName, DataRecord input) {
        DeployedFunction deployed = store.findLatest(objectPath, functionName)
                .orElseThrow(() -> new IllegalStateException("Deployed function missing: " + functionName));

        ensureDescriptor(objectPath, functionName, deployed);

        DataSchema outputSchema = readSchema(deployed.outputSchemaJson());
        DataRecord output;
        try {
            output = switch (deployed.sourceType()) {
                case "script" -> scriptEngine.execute(deployed.sourceBody(), input, outputSchema);
                default -> throw new IllegalStateException("Unsupported source type: " + deployed.sourceType());
            };
            auditService.record(deployed.appId(), objectPath, functionName, true, null);
            return output;
        } catch (RuntimeException ex) {
            auditService.record(deployed.appId(), objectPath, functionName, false, ex.getMessage());
            throw ex;
        }
    }

    private void ensureDescriptor(String objectPath, String functionName, DeployedFunction deployed) {
        var node = objectManager.require(objectPath);
        if (!node.functions().containsKey(functionName)) {
            DataSchema inputSchema = readSchema(deployed.inputSchemaJson());
            node.addFunction(new FunctionDescriptor(
                    functionName,
                    "Application function " + functionName,
                    inputSchema,
                    readSchema(deployed.outputSchemaJson())
            ));
            objectManager.persistNodeTree(objectPath);
        }
    }

    private DataSchema readSchema(String json) {
        try {
            return objectMapper.readValue(json, DataSchema.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid function schema JSON", ex);
        }
    }

    public record DeployedFunction(
            UUID id,
            String appId,
            String objectPath,
            String functionName,
            String version,
            String sourceType,
            String sourceBody,
            String inputSchemaJson,
            String outputSchemaJson
    ) {
    }
}
