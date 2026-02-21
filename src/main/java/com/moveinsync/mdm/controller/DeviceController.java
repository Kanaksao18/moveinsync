package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.DeviceHeartbeatRequest;
import com.moveinsync.mdm.dto.HeartbeatResponse;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/heartbeat")
    public HeartbeatResponse heartbeat(@RequestBody DeviceHeartbeatRequest request) {
        return deviceService.heartbeat(request);
    }
}