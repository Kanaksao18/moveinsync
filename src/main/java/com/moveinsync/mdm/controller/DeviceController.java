package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.DeviceHeartbeatRequest;
import com.moveinsync.mdm.dto.DashboardSummaryResponse;
import com.moveinsync.mdm.dto.HeartbeatResponse;
import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // Device heartbeat (mobile app calls this)
    @PostMapping("/heartbeat")
    public HeartbeatResponse heartbeat(@RequestBody DeviceHeartbeatRequest request) {
        return deviceService.heartbeat(request);
    }

    // Dashboard: fetch all devices
    @GetMapping
    public Page<Device> getAllDevices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String appVersion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastOpenTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return deviceService.getDevices(search, region, active, appVersion, pageable);
    }

    @PostMapping
    public Device create(@RequestBody DeviceHeartbeatRequest request) {
        return deviceService.addOrUpdateDevice(request);
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return deviceService.getDashboardSummary();
    }

    @GetMapping("/inactive")
    public List<Device> inactive(@RequestParam(defaultValue = "48") int hours) {
        return deviceService.getInactiveDevices(hours);
    }

    @GetMapping("/{deviceId}/timeline")
    public List<AuditLog> timeline(@PathVariable Long deviceId) {
        return deviceService.getDeviceTimeline(deviceId);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 200);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
