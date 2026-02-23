package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.DeviceUpdateMonitoringRow;
import com.moveinsync.mdm.dto.UpdateStateRequest;
import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.entity.UpdateSchedule;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.exception.ResourceNotFoundException;
import com.moveinsync.mdm.repository.AuditLogRepository;
import com.moveinsync.mdm.repository.DeviceRepository;
import com.moveinsync.mdm.repository.DeviceUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceUpdateService {

    private final DeviceUpdateRepository deviceUpdateRepository;
    private final AuditLogRepository auditLogRepository;
    private final UpdateScheduleService updateScheduleService;
    private final DeviceRepository deviceRepository;

    public void updateState(UpdateStateRequest request) {
        DeviceUpdate update = deviceUpdateRepository.findById(request.getDeviceUpdateId())
                .orElseThrow(() -> new ResourceNotFoundException("Device update not found"));

        String state = request.getState() != null ? request.getState().trim().toUpperCase() : "";
        if (state.isEmpty()) {
            throw new BadRequestException("State is required");
        }

        UpdateSchedule schedule = update.getSchedule();
        Device device = update.getDevice();

        // State transitions capture stage timestamps and enforce rollout safety checks.
        switch (state) {
            case "DEVICE_NOTIFIED" -> update.setNotifiedAt(LocalDateTime.now());
            case "DOWNLOAD_STARTED" -> update.setDownloadStartedAt(LocalDateTime.now());
            case "DOWNLOAD_COMPLETED" -> update.setDownloadCompletedAt(LocalDateTime.now());
            case "INSTALL_STARTED" -> {
                validateNoDowngrade(device, schedule);
                update.setInstallStartedAt(LocalDateTime.now());
            }
            case "COMPLETED" -> {
                validateNoDowngrade(device, schedule);
                update.setInstallCompletedAt(LocalDateTime.now());
                device.setAppVersion(schedule.getToVersion());
                deviceRepository.save(device);
            }
            case "FAILED" -> {
                update.setFailedAt(LocalDateTime.now());
                update.setFailureStage(inferFailureStage(update));
                update.setFailureReason(request.getFailureReason());
                // Retry is scheduled using schedule-level backoff + max retry limits.
                applyRetryPolicy(update);
            }
            default -> {
            }
        }

        update.setState(state);
        deviceUpdateRepository.save(update);
        logAudit("DEVICE_UPDATE_" + state, update, request.getFailureReason());

        if ("FAILED".equals(state)) {
            updateScheduleService.evaluateRolloutHealth(schedule.getId());
        }
    }

    public int retryFailedDevices(Long scheduleId) {
        List<DeviceUpdate> failed = deviceUpdateRepository.findByScheduleAndState(
                updateScheduleService.getSchedule(scheduleId),
                "FAILED"
        );

        for (DeviceUpdate update : failed) {
            update.setState("SCHEDULED");
            update.setFailureReason(null);
            update.setFailureStage(null);
            update.setNextRetryAt(null);
            update.setRetryCount((update.getRetryCount() != null ? update.getRetryCount() : 0) + 1);
            deviceUpdateRepository.save(update);
            logAudit("DEVICE_UPDATE_RETRY_MANUAL", update, "Manual retry");
        }

        return failed.size();
    }

    public List<DeviceUpdateMonitoringRow> getMonitoringRows(Long scheduleId) {
        return deviceUpdateRepository.findByScheduleId(scheduleId).stream()
                .map(update -> new DeviceUpdateMonitoringRow(
                        update.getId(),
                        update.getDevice() != null ? update.getDevice().getImei() : null,
                        update.getDevice() != null ? update.getDevice().getId() : null,
                        update.getState(),
                        update.getFailureReason()
                ))
                .toList();
    }

    @Scheduled(fixedDelay = 60000)
    public void retryEligibleFailedUpdates() {
        // Background worker retries only failed entries whose nextRetryAt has elapsed.
        List<DeviceUpdate> eligible = deviceUpdateRepository.findByStateAndNextRetryAtLessThanEqual("FAILED", LocalDateTime.now());
        for (DeviceUpdate update : eligible) {
            int retries = update.getRetryCount() != null ? update.getRetryCount() : 0;
            int maxRetries = update.getSchedule().getMaxRetries() != null ? update.getSchedule().getMaxRetries() : 2;
            if (retries >= maxRetries) {
                continue;
            }

            update.setState("SCHEDULED");
            update.setRetryCount(retries + 1);
            update.setFailureReason(null);
            update.setFailureStage(null);
            update.setNextRetryAt(null);
            deviceUpdateRepository.save(update);
            logAudit("DEVICE_UPDATE_RETRY_AUTO", update, "Auto retry policy executed");
        }
    }

    private void applyRetryPolicy(DeviceUpdate update) {
        int retries = update.getRetryCount() != null ? update.getRetryCount() : 0;
        int maxRetries = update.getSchedule().getMaxRetries() != null ? update.getSchedule().getMaxRetries() : 2;
        if (retries >= maxRetries) {
            // Retry budget exhausted: keep record failed and stop scheduling retries.
            update.setNextRetryAt(null);
            return;
        }
        int backoff = update.getSchedule().getRetryBackoffMinutes() != null ? update.getSchedule().getRetryBackoffMinutes() : 10;
        update.setNextRetryAt(LocalDateTime.now().plusMinutes(backoff));
    }

    private String inferFailureStage(DeviceUpdate update) {
        // Infer nearest stage reached before failure for better diagnostics.
        if (update.getInstallStartedAt() != null) {
            return "INSTALL";
        }
        if (update.getDownloadStartedAt() != null) {
            return "DOWNLOAD";
        }
        if (update.getNotifiedAt() != null) {
            return "NOTIFICATION";
        }
        return "SCHEDULE";
    }

    private void validateNoDowngrade(Device device, UpdateSchedule schedule) {
        // Defensive check: even if a bad schedule slips through, block downgrade at execution time.
        try {
            double current = Double.parseDouble(device.getAppVersion());
            double target = Double.parseDouble(schedule.getToVersion());
            if (target < current) {
                throw new BadRequestException("Downgrade blocked at device validation stage");
            }
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid version format for downgrade validation");
        }
    }

    private void logAudit(String action, DeviceUpdate update, String details) {
        AuditLog log = new AuditLog();
        log.setActor("DEVICE");
        log.setAction(action);
        log.setDetails(details);
        log.setScheduleId(update.getSchedule() != null ? update.getSchedule().getId() : null);
        log.setDeviceId(update.getDevice() != null ? update.getDevice().getId() : null);
        log.setDeviceImei(update.getDevice() != null ? update.getDevice().getImei() : null);
        auditLogRepository.save(log);
    }
}
