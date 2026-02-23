package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<AuditLog> list(@RequestParam(required = false) Long deviceId,
                               @RequestParam(required = false) Long scheduleId) {
        if (deviceId != null) {
            return auditLogRepository.findByDeviceIdOrderByTimestampAsc(deviceId);
        }
        if (scheduleId != null) {
            return auditLogRepository.findByScheduleIdOrderByTimestampAsc(scheduleId);
        }
        return auditLogRepository.findAll();
    }
}
