package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.DeviceHeartbeatRequest;
import com.moveinsync.mdm.dto.DashboardSummaryResponse;
import com.moveinsync.mdm.dto.HeartbeatResponse;
import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.service.DeviceService;
import lombok.RequiredArgsConstructor;
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
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
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
}
