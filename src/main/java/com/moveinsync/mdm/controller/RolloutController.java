package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.service.DeviceUpdateService;
import com.moveinsync.mdm.service.UpdateScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rollout")
@RequiredArgsConstructor
public class RolloutController {

    private final UpdateScheduleService service;
    private final DeviceUpdateService deviceUpdateService;

    @PostMapping("/{scheduleId}/next")
    public String nextPhase(@PathVariable Long scheduleId) {
        service.nextPhase(scheduleId);
        return "Next phase started";
    }
    @PostMapping("/{scheduleId}/retry")
    public String retry(@PathVariable Long scheduleId) {

        int count = deviceUpdateService.retryFailedDevices(scheduleId);
        return count + " devices retried";
    }
}