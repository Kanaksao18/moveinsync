package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.DeviceUpdateMonitoringRow;
import com.moveinsync.mdm.dto.UpdateStateRequest;
import com.moveinsync.mdm.service.DeviceUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-update")
@RequiredArgsConstructor
public class DeviceUpdateController {

    private final DeviceUpdateService service;

    @PostMapping("/state")
    public String updateState(@RequestBody UpdateStateRequest request) {
        service.updateState(request);
        return "State Updated";
    }

    @GetMapping
    public Page<DeviceUpdateMonitoringRow> getBySchedule(
            @RequestParam Long scheduleId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return service.getMonitoringRows(scheduleId, state, search, pageable);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 200);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
