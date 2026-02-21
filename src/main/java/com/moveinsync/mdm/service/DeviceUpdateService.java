package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.UpdateStateRequest;
import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.exception.ResourceNotFoundException;
import com.moveinsync.mdm.repository.AuditLogRepository;
import com.moveinsync.mdm.repository.DeviceUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceUpdateService {

    private final DeviceUpdateRepository deviceUpdateRepository;
    private final AuditLogRepository auditLogRepository;
    private final UpdateScheduleService updateScheduleService;   // 👈 ADD THIS

    public void updateState(UpdateStateRequest request) {

        DeviceUpdate du = deviceUpdateRepository.findById(request.getDeviceUpdateId())
                .orElseThrow(() -> new ResourceNotFoundException("Device update not found"));

        du.setState(request.getState());
        du.setFailureReason(request.getFailureReason());

        deviceUpdateRepository.save(du);

        // ✅ Audit log
        AuditLog log = new AuditLog();
        log.setActor("DEVICE");
        log.setAction(request.getState());
        log.setDetails("DeviceUpdateId: " + du.getId());
        auditLogRepository.save(log);

        // 🔥 If device failed → check rollout health
        if ("FAILED".equalsIgnoreCase(request.getState())) {
            updateScheduleService.evaluateRolloutHealth(
                    du.getSchedule().getId()
            );
        }
    }
    public int retryFailedDevices(Long scheduleId) {

        List<DeviceUpdate> failed =
                deviceUpdateRepository.findByScheduleAndState(
                        updateScheduleService.getSchedule(scheduleId),
                        "FAILED"
                );

        for (DeviceUpdate du : failed) {
            du.setState("SCHEDULED");
            du.setFailureReason(null);
            deviceUpdateRepository.save(du);

            AuditLog log = new AuditLog();
            log.setActor("SYSTEM");
            log.setAction("RETRY");
            log.setDetails("Retry DeviceUpdateId: " + du.getId());
            auditLogRepository.save(log);
        }

        return failed.size();
    }
}