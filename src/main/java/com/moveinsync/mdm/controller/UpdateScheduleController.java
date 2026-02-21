package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.UpdateScheduleRequest;
import com.moveinsync.mdm.entity.UpdateSchedule;
import com.moveinsync.mdm.service.UpdateScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/update")
@RequiredArgsConstructor
public class UpdateScheduleController {

    private final UpdateScheduleService service;

    @PostMapping("/schedule")
    public UpdateSchedule schedule(@RequestBody UpdateScheduleRequest request) {
        return service.schedule(request);
    }
}