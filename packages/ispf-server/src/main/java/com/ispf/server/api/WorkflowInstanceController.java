package com.ispf.server.api;

import com.ispf.server.workflow.WorkflowInstanceCancelService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflows/instances")
public class WorkflowInstanceController {

    private final WorkflowInstanceCancelService cancelService;

    public WorkflowInstanceController(WorkflowInstanceCancelService cancelService) {
        this.cancelService = cancelService;
    }

    @PostMapping("/{instanceId}/cancel")
    public Map<String, Object> cancel(
            @PathVariable String instanceId,
            @RequestBody CancelWorkflowRequest request
    ) {
        return cancelService.cancel(
                instanceId,
                request.reason(),
                request.detailJson(),
                request.cancelledBy()
        );
    }

    public record CancelWorkflowRequest(String reason, String detailJson, String cancelledBy) {
    }
}
