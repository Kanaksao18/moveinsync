package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.UpdateScheduleRequest;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.entity.UpdateSchedule;
import com.moveinsync.mdm.repository.DeviceRepository;
import com.moveinsync.mdm.repository.DeviceUpdateRepository;
import com.moveinsync.mdm.repository.UpdateScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateScheduleService {

    private final UpdateScheduleRepository scheduleRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceUpdateRepository deviceUpdateRepository;
    private final VersionCompatibilityService compatibilityService;

    public UpdateSchedule schedule(UpdateScheduleRequest request) {

        // Enterprise upgrade validation
        compatibilityService.validateUpgrade(
                request.getFromVersion(),
                request.getToVersion()
        );

        UpdateSchedule schedule = new UpdateSchedule();
        schedule.setFromVersion(request.getFromVersion());
        schedule.setToVersion(request.getToVersion());
        schedule.setRegion(request.getRegion());
        schedule.setRolloutType(request.getRolloutType());
        schedule.setPercentage(request.getPercentage());
        schedule.setScheduledTime(LocalDateTime.now());
        schedule.setBatchPercentage(request.getBatchPercentage());
        schedule.setBatchNumber(1);

        scheduleRepository.save(schedule);

        rolloutDevices(schedule, request.getBatchPercentage());

        return schedule;
    }

    /**
     * Execute next rollout phase
     */
    public void nextPhase(Long scheduleId) {

        UpdateSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow();

        schedule.setBatchNumber(schedule.getBatchNumber() + 1);
        scheduleRepository.save(schedule);

        rolloutDevices(schedule, schedule.getBatchPercentage());
    }

    /**
     * Core rollout logic
     */
    private void rolloutDevices(UpdateSchedule schedule, Integer batchPercentage) {

        List<Device> remainingDevices =
                deviceRepository.findAll().stream()
                        .filter(d -> d.getRegion().equalsIgnoreCase(schedule.getRegion()))
                        .filter(d -> d.getAppVersion().equals(schedule.getFromVersion()))
                        .filter(d -> deviceUpdateRepository
                                .findByDeviceAndSchedule(d, schedule).isEmpty())
                        .toList();

        int total = remainingDevices.size();

        if (total == 0) return;

        int rolloutCount = schedule.getRolloutType().equals("PHASED")
                ? (total * batchPercentage / 100)
                : total;

        List<Device> selected = remainingDevices.stream()
                .limit(rolloutCount)
                .toList();

        for (Device device : selected) {

            DeviceUpdate du = new DeviceUpdate();
            du.setDevice(device);
            du.setSchedule(schedule);
            du.setState("SCHEDULED");

            deviceUpdateRepository.save(du);
        }
    }
    public void evaluateRolloutHealth(Long scheduleId) {

        UpdateSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow();

        long total = deviceUpdateRepository.countBySchedule(schedule);
        long failed = deviceUpdateRepository.countByScheduleAndState(schedule, "FAILED");

        if (total == 0) return;

        int failurePercent = (int) ((failed * 100) / total);

        if (failurePercent >= schedule.getFailureThreshold()) {
            triggerRollback(schedule);
        }
    }
    private void triggerRollback(UpdateSchedule schedule) {

        schedule.setStatus("ROLLED_BACK");
        scheduleRepository.save(schedule);

        List<DeviceUpdate> targets =
                schedule.getRollbackScope().equals("FULL")
                        ? deviceUpdateRepository.findBySchedule(schedule)
                        : deviceUpdateRepository.findByScheduleAndState(schedule, "FAILED");

        for (DeviceUpdate du : targets) {

            Device device = du.getDevice();
            device.setAppVersion(schedule.getFromVersion());
            deviceRepository.save(device);

            du.setState("ROLLED_BACK");
            deviceUpdateRepository.save(du);
        }

        System.out.println("🔥 Rollback executed (" + schedule.getRollbackScope() + ")");
    }
    public UpdateSchedule getSchedule(Long id) {
        return scheduleRepository.findById(id).orElseThrow();
    }
}