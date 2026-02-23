package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.RolloutProgressRow;
import com.moveinsync.mdm.dto.UpdateScheduleRequest;
import com.moveinsync.mdm.entity.AppVersion;
import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.entity.UpdateSchedule;
import com.moveinsync.mdm.event.DeviceNotificationEvent;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.exception.ResourceNotFoundException;
import com.moveinsync.mdm.repository.AppVersionRepository;
import com.moveinsync.mdm.repository.AuditLogRepository;
import com.moveinsync.mdm.repository.DeviceRepository;
import com.moveinsync.mdm.repository.DeviceUpdateRepository;
import com.moveinsync.mdm.repository.UpdateScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdateScheduleService {

    private final UpdateScheduleRepository scheduleRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceUpdateRepository deviceUpdateRepository;
    private final VersionCompatibilityService compatibilityService;
    private final AppVersionRepository appVersionRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateSchedule schedule(UpdateScheduleRequest request, Authentication authentication) {
        // Guard rails: block downgrade and disallow incompatible upgrade paths up front.
        validateDowngradeRestriction(request.getFromVersion(), request.getToVersion());
        compatibilityService.validateUpgrade(request.getFromVersion(), request.getToVersion());

        AppVersion targetVersion = appVersionRepository.findByVersionCode(request.getToVersion())
                .orElseThrow(() -> new BadRequestException("Target version does not exist"));

        UpdateSchedule schedule = new UpdateSchedule();
        schedule.setFromVersion(request.getFromVersion());
        schedule.setToVersion(request.getToVersion());
        schedule.setRegion(request.getRegion());
        schedule.setCustomizationTag(request.getCustomizationTag());
        schedule.setDeviceGroup(request.getDeviceGroup());
        schedule.setRolloutType(request.getRolloutType() != null ? request.getRolloutType() : "IMMEDIATE");
        schedule.setPercentage(request.getPercentage());
        schedule.setBatchPercentage(request.getBatchPercentage() != null ? request.getBatchPercentage() : 100);
        schedule.setBatchNumber(1);
        schedule.setRollbackScope(request.getRollbackScope() != null ? request.getRollbackScope() : "FAILED_ONLY");
        schedule.setFailureThreshold(request.getFailureThreshold() != null ? request.getFailureThreshold() : 25);
        schedule.setMaxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 2);
        schedule.setRetryBackoffMinutes(request.getRetryBackoffMinutes() != null ? request.getRetryBackoffMinutes() : 10);
        schedule.setMandatoryUpdate(Boolean.TRUE.equals(targetVersion.getMandatory()));
        schedule.setCreatedBy(authentication != null ? authentication.getName() : "SYSTEM");

        LocalDateTime scheduleTime = request.getScheduledTime() != null ? request.getScheduledTime() : LocalDateTime.now();
        schedule.setScheduledTime(scheduleTime);

        boolean requiresApproval = Boolean.TRUE.equals(schedule.getMandatoryUpdate());
        // Mandatory rollouts go through approval even if immediately scheduled.
        if (scheduleTime.isAfter(LocalDateTime.now())) {
            schedule.setStatus(requiresApproval ? "PENDING_APPROVAL" : "SCHEDULED");
        } else {
            schedule.setStatus(requiresApproval ? "PENDING_APPROVAL" : "ACTIVE");
        }

        scheduleRepository.save(schedule);
        logAudit(
                "SCHEDULE_CREATED",
                schedule,
                null,
                "by=" + schedule.getCreatedBy()
                        + ", from=" + schedule.getFromVersion()
                        + ", to=" + schedule.getToVersion()
                        + ", region=" + schedule.getRegion()
                        + ", customizationTag=" + schedule.getCustomizationTag()
                        + ", deviceGroup=" + schedule.getDeviceGroup()
        );

        if (Objects.equals(schedule.getStatus(), "ACTIVE")) {
            rolloutDevices(schedule, schedule.getBatchPercentage());
        }

        return schedule;
    }

    public UpdateSchedule approve(Long scheduleId, Authentication authentication) {
        UpdateSchedule schedule = getSchedule(scheduleId);
        if (!Objects.equals(schedule.getStatus(), "PENDING_APPROVAL")) {
            throw new BadRequestException("Schedule is not pending approval");
        }

        schedule.setApprovedBy(authentication != null ? authentication.getName() : "SYSTEM");
        schedule.setApprovedAt(LocalDateTime.now());
        schedule.setStatus(schedule.getScheduledTime() != null && schedule.getScheduledTime().isAfter(LocalDateTime.now())
                ? "SCHEDULED"
                : "ACTIVE");
        scheduleRepository.save(schedule);

        logAudit("SCHEDULE_APPROVED", schedule, null, "Approved by " + schedule.getApprovedBy());
        if (Objects.equals(schedule.getStatus(), "ACTIVE")) {
            rolloutDevices(schedule, schedule.getBatchPercentage());
        }
        return schedule;
    }

    public UpdateSchedule reject(Long scheduleId, Authentication authentication, String reason) {
        UpdateSchedule schedule = getSchedule(scheduleId);
        if (!Objects.equals(schedule.getStatus(), "PENDING_APPROVAL")) {
            throw new BadRequestException("Schedule is not pending approval");
        }
        schedule.setStatus("REJECTED");
        schedule.setApprovedBy(authentication != null ? authentication.getName() : "SYSTEM");
        schedule.setApprovedAt(LocalDateTime.now());
        scheduleRepository.save(schedule);
        logAudit("SCHEDULE_REJECTED", schedule, null, reason != null ? reason : "Rejected");
        return schedule;
    }

    public void nextPhase(Long scheduleId) {
        // PHASED rollout advances by increasing batch number and scheduling next eligible slice.
        UpdateSchedule schedule = getSchedule(scheduleId);
        schedule.setBatchNumber(schedule.getBatchNumber() + 1);
        scheduleRepository.save(schedule);
        rolloutDevices(schedule, schedule.getBatchPercentage());
    }

    private void rolloutDevices(UpdateSchedule schedule, Integer batchPercentage) {
        AppVersion targetVersion = appVersionRepository.findByVersionCode(schedule.getToVersion())
                .orElse(null);

        // Current implementation filters in memory for simplicity/readability.
        List<Device> remainingDevices = deviceRepository.findAll().stream()
                .filter(d -> schedule.getRegion() == null
                        || (d.getRegion() != null && d.getRegion().equalsIgnoreCase(schedule.getRegion())))
                .filter(d -> schedule.getCustomizationTag() == null || Objects.equals(d.getCustomizationTag(), schedule.getCustomizationTag()))
                .filter(d -> schedule.getDeviceGroup() == null || Objects.equals(d.getDeviceGroup(), schedule.getDeviceGroup()))
                .filter(d -> Objects.equals(d.getAppVersion(), schedule.getFromVersion()))
                .filter(d -> isDeviceOsSupported(d.getOs(), targetVersion != null ? targetVersion.getSupportedOs() : null))
                .filter(d -> deviceUpdateRepository.findByDeviceAndSchedule(d, schedule).isEmpty())
                .toList();

        int total = remainingDevices.size();
        if (total == 0) {
            return;
        }

        int rolloutCount = Objects.equals(schedule.getRolloutType(), "PHASED")
                ? Math.max(1, (total * batchPercentage / 100))
                : total;

        List<Device> selected = remainingDevices.stream().limit(rolloutCount).toList();

        for (Device device : selected) {
            DeviceUpdate update = new DeviceUpdate();
            update.setDevice(device);
            update.setSchedule(schedule);
            update.setState("SCHEDULED");
            deviceUpdateRepository.save(update);

            logAudit("DEVICE_UPDATE_SCHEDULED", schedule, device, "Device update queued");
            // Notification dispatch is async and post-commit through an application event listener.
            eventPublisher.publishEvent(new DeviceNotificationEvent(update.getId()));
        }
    }

    public void evaluateRolloutHealth(Long scheduleId) {
        UpdateSchedule schedule = getSchedule(scheduleId);

        long total = deviceUpdateRepository.countBySchedule(schedule);
        long failed = deviceUpdateRepository.countByScheduleAndState(schedule, "FAILED");

        if (total == 0) {
            schedule.setFailurePercentage(0);
            scheduleRepository.save(schedule);
            return;
        }

        int failurePercent = (int) ((failed * 100) / total);
        schedule.setFailurePercentage(failurePercent);
        scheduleRepository.save(schedule);

        int threshold = schedule.getFailureThreshold() != null ? schedule.getFailureThreshold() : 25;
        // Rollback is automatically triggered once configured failure threshold is crossed.
        if (failurePercent >= threshold) {
            triggerRollback(schedule);
        }
    }

    private void triggerRollback(UpdateSchedule schedule) {
        schedule.setStatus("ROLLED_BACK");
        scheduleRepository.save(schedule);
        logAudit("ROLLBACK_TRIGGERED", schedule, null, "Rollback scope " + schedule.getRollbackScope());

        List<DeviceUpdate> targets = "FULL".equals(schedule.getRollbackScope())
                ? deviceUpdateRepository.findBySchedule(schedule)
                : deviceUpdateRepository.findByScheduleAndState(schedule, "FAILED");

        for (DeviceUpdate update : targets) {
            Device device = update.getDevice();
            device.setAppVersion(schedule.getFromVersion());
            deviceRepository.save(device);

            update.setState("ROLLED_BACK");
            deviceUpdateRepository.save(update);
            logAudit("DEVICE_ROLLED_BACK", schedule, device, "Rollback applied");
        }
    }

    public UpdateSchedule getSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
    }

    public Page<UpdateSchedule> getAllSchedules(
            String status,
            String region,
            String fromVersion,
            String toVersion,
            Pageable pageable
    ) {
        Specification<UpdateSchedule> spec = Specification.<UpdateSchedule>where((Specification<UpdateSchedule>) null);

        if (status != null && !status.isBlank()) {
            String value = status.trim().toUpperCase();
            spec = spec.and((root, q, cb) -> cb.equal(cb.upper(root.get("status")), value));
        }

        if (region != null && !region.isBlank()) {
            String query = "%" + region.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("region")), query));
        }

        if (fromVersion != null && !fromVersion.isBlank()) {
            String query = "%" + fromVersion.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("fromVersion")), query));
        }

        if (toVersion != null && !toVersion.isBlank()) {
            String query = "%" + toVersion.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("toVersion")), query));
        }

        return scheduleRepository.findAll(spec, pageable);
    }

    public List<RolloutProgressRow> getRolloutProgress() {
        return scheduleRepository.findAll().stream().map(this::toProgressRow).toList();
    }

    private RolloutProgressRow toProgressRow(UpdateSchedule schedule) {
        long total = deviceUpdateRepository.countBySchedule(schedule);
        long completed = deviceUpdateRepository.countByScheduleAndState(schedule, "COMPLETED");
        long failed = deviceUpdateRepository.countByScheduleAndState(schedule, "FAILED");
        int successRate = total == 0 ? 0 : (int) ((completed * 100) / total);
        int failureRate = total == 0 ? 0 : (int) ((failed * 100) / total);

        return new RolloutProgressRow(
                schedule.getId(),
                schedule.getFromVersion(),
                schedule.getToVersion(),
                schedule.getRegion(),
                schedule.getStatus(),
                total,
                completed,
                failed,
                successRate,
                failureRate
        );
    }

    @Scheduled(fixedDelay = 60000)
    public void processDueSchedules() {
        // Activates future schedules once their scheduledTime is reached.
        List<UpdateSchedule> due = scheduleRepository.findByStatusAndScheduledTimeLessThanEqual("SCHEDULED", LocalDateTime.now());
        for (UpdateSchedule schedule : due) {
            schedule.setStatus("ACTIVE");
            scheduleRepository.save(schedule);
            logAudit("SCHEDULE_ACTIVATED", schedule, null, "Scheduled time reached");
            rolloutDevices(schedule, schedule.getBatchPercentage());
        }
    }

    private void validateDowngradeRestriction(String fromVersion, String toVersion) {
        try {
            double from = Double.parseDouble(fromVersion);
            double to = Double.parseDouble(toVersion);
            if (to < from) {
                throw new BadRequestException("Downgrade scheduling is not allowed");
            }
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Version values must be numeric for downgrade validation");
        }
    }

    private boolean isDeviceOsSupported(String deviceOs, String supportedSpec) {
        // Simple numeric compatibility rule (e.g., "Android >=12" -> 12).
        if (supportedSpec == null || supportedSpec.isBlank()) {
            return true;
        }
        if (deviceOs == null || deviceOs.isBlank()) {
            return false;
        }

        Integer required = extractFirstNumber(supportedSpec);
        Integer current = extractFirstNumber(deviceOs);
        if (required == null || current == null) {
            return true;
        }
        return current >= required;
    }

    private Integer extractFirstNumber(String text) {
        StringBuilder digits = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (Character.isDigit(ch)) {
                digits.append(ch);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.isEmpty()) {
            return null;
        }
        return Integer.parseInt(digits.toString());
    }

    private void logAudit(String action, UpdateSchedule schedule, Device device, String details) {
        AuditLog log = new AuditLog();
        log.setActor("SYSTEM");
        log.setAction(action);
        log.setDetails(details);
        log.setScheduleId(schedule != null ? schedule.getId() : null);
        if (device != null) {
            log.setDeviceId(device.getId());
            log.setDeviceImei(device.getImei());
        }
        auditLogRepository.save(log);
    }
}
