package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.UpdateStateRequest;
import com.moveinsync.mdm.service.DeviceUpdateService;
import lombok.RequiredArgsConstructor;
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
}