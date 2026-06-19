package com.ispf.server.application.bff;

import com.ispf.core.model.DataRecord;
import com.ispf.server.function.FunctionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bff")
public class BffController {

    private final FunctionService functionService;

    public BffController(FunctionService functionService) {
        this.functionService = functionService;
    }

    @PostMapping("/invoke")
    public Map<String, Object> invoke(@RequestBody BffInvokeRequest request) {
        DataRecord output = functionService.invoke(
                request.objectPath(),
                request.functionName(),
                request.input()
        );
        return BffWireMapper.toWire(output, request.wireProfile());
    }

    public record BffInvokeRequest(
            String objectPath,
            String functionName,
            DataRecord input,
            String wireProfile
    ) {
    }

    static final class BffWireMapper {

        private BffWireMapper() {
        }

        static Map<String, Object> toWire(DataRecord output, String wireProfile) {
            Map<String, Object> row = output != null && output.rowCount() > 0
                    ? new LinkedHashMap<>(output.firstRow())
                    : new LinkedHashMap<>();

            String errorCode = stringValue(row.remove("error_code"), "OK");
            String errorMessage = stringValue(row.remove("error_message"), "");

            Map<String, Object> wire = new LinkedHashMap<>();
            wire.put("error_code", errorCode);
            wire.put("error_message", errorMessage);
            if ("OK".equals(errorCode)) {
                wire.put("result", row);
            }
            if (wireProfile != null && !wireProfile.isBlank()) {
                wire.put("wireProfile", wireProfile);
            }
            return wire;
        }

        private static String stringValue(Object value, String fallback) {
            return value == null ? fallback : String.valueOf(value);
        }
    }
}
