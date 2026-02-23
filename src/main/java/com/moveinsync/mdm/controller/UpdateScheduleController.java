package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.UpdateScheduleRequest;
import com.moveinsync.mdm.entity.UpdateSchedule;
import com.moveinsync.mdm.service.UpdateScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UpdateScheduleController {

    private final UpdateScheduleService service;

    @PostMapping({"/api/update/schedule", "/api/schedule"})
    public UpdateSchedule schedule(@RequestBody UpdateScheduleRequest request, Authentication authentication) {
        return service.schedule(request, authentication);
    }

    @GetMapping("/api/schedule")
    public Page<UpdateSchedule> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String fromVersion,
            @RequestParam(required = false) String toVersion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "scheduledTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return service.getAllSchedules(status, region, fromVersion, toVersion, pageable);
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

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 200);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
