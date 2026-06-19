package com.ispf.server.workflow;

import com.ispf.plugin.workflow.InstanceStatus;
import com.ispf.server.persistence.WorkflowInstanceRepository;
import com.ispf.server.persistence.entity.WorkflowInstanceEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowInstanceCancelService {

    private final WorkflowInstanceRepository instanceRepository;
    private final JdbcTemplate jdbcTemplate;

    public WorkflowInstanceCancelService(
            WorkflowInstanceRepository instanceRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.instanceRepository = instanceRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Map<String, Object> cancel(String instanceId, String reason, String detailJson, String cancelledBy) {
        WorkflowInstanceEntity entity = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow instance not found: " + instanceId));

        if (InstanceStatus.FAILED.name().equals(entity.getStatus())
                || InstanceStatus.COMPLETED.name().equals(entity.getStatus())) {
            return Map.of(
                    "instanceId", instanceId,
                    "status", entity.getStatus(),
                    "cancelled", false,
                    "message", "Already terminal"
            );
        }

        entity.setStatus(InstanceStatus.FAILED.name());
        entity.setUpdatedAt(Instant.now());
        instanceRepository.save(entity);

        jdbcTemplate.update("""
                INSERT INTO workflow_cancel_journal (
                    id, instance_id, workflow_path, reason, detail_json, cancelled_by, cancelled_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                instanceId,
                entity.getWorkflowPath(),
                reason,
                detailJson,
                cancelledBy,
                Timestamp.from(Instant.now())
        );

        return Map.of(
                "instanceId", instanceId,
                "status", InstanceStatus.FAILED.name(),
                "cancelled", true,
                "reason", reason
        );
    }

    @Transactional
    public int cancelWaitingByWorkflowPath(String workflowPath, String reason, String detailJson, String cancelledBy) {
        List<WorkflowInstanceEntity> waiting = instanceRepository.findByWorkflowPathAndStatus(
                workflowPath,
                InstanceStatus.WAITING.name()
        );
        int count = 0;
        for (WorkflowInstanceEntity entity : waiting) {
            cancel(entity.getId(), reason, detailJson, cancelledBy);
            count++;
        }
        return count;
    }
}
