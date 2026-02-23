package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.DashboardSummaryResponse;
import com.moveinsync.mdm.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DeviceService deviceService;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return deviceService.getDashboardSummary();
    }
}
