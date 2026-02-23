package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.UpdateScheduleRequest;
import com.moveinsync.mdm.entity.UpdateSchedule;
import com.moveinsync.mdm.service.UpdateScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UpdateScheduleController {

    private final UpdateScheduleService service;

    @PostMapping({"/api/update/schedule", "/api/schedule"})
    public UpdateSchedule schedule(@RequestBody UpdateScheduleRequest request, Authentication authentication) {
        return service.schedule(request, authentication);
    }

    @GetMapping("/api/schedule")
    public List<UpdateSchedule> list() {
        return service.getAllSchedules();
    }

    @PostMapping("/api/schedule/{id}/approve")
    public UpdateSchedule approve(@PathVariable Long id, Authentication authentication) {
        return service.approve(id, authentication);
    }

    @PostMapping("/api/schedule/{id}/reject")
    public UpdateSchedule reject(@PathVariable Long id,
                                 @RequestParam(required = false) String reason,
                                 Authentication authentication) {
        return service.reject(id, authentication, reason);
    }
}
