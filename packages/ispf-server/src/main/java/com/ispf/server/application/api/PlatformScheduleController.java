package com.ispf.server.application.api;

import com.ispf.server.application.schedule.PlatformSchedulerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedules")
public class PlatformScheduleController {

    private final PlatformSchedulerService schedulerService;

    public PlatformScheduleController(PlatformSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return schedulerService.list();
    }

    @PostMapping
    public Map<String, Object> upsert(@RequestBody ScheduleRequest request) {
        schedulerService.upsert(new PlatformSchedulerService.PlatformSchedule(
                request.scheduleId(),
                request.appId(),
                request.enabled(),
                request.intervalMs(),
                request.action().type(),
                request.action().json()
        ));
        return Map.of("scheduleId", request.scheduleId(), "status", "saved");
    }

    public record ScheduleRequest(
            String scheduleId,
            String appId,
            boolean enabled,
            long intervalMs,
            ScheduleActionRequest action
    ) {
    }

    public record ScheduleActionRequest(String type, String json) {
    }
}
