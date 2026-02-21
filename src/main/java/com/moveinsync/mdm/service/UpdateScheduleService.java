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

    public UpdateSchedule schedule(UpdateScheduleRequest request) {

        // Downgrade prevention
        if (Double.parseDouble(request.getToVersion()) <
                Double.parseDouble(request.getFromVersion())) {
            throw new RuntimeException("Downgrade not allowed");
        }

        UpdateSchedule schedule = new UpdateSchedule();
        schedule.setFromVersion(request.getFromVersion());
        schedule.setToVersion(request.getToVersion());
        schedule.setRegion(request.getRegion());
        schedule.setRolloutType(request.getRolloutType());
        schedule.setPercentage(request.getPercentage());
        schedule.setScheduledTime(LocalDateTime.now());

        scheduleRepository.save(schedule);

        // Fetch target devices
        List<Device> devices =
                deviceRepository.findAll().stream()
                        .filter(d -> d.getRegion().equalsIgnoreCase(request.getRegion()))
                        .filter(d -> d.getAppVersion().equals(request.getFromVersion()))
                        .toList();

        // Create rollout records
        for (Device device : devices) {
            DeviceUpdate du = new DeviceUpdate();
            du.setDevice(device);
            du.setSchedule(schedule);
            du.setState("SCHEDULED");
            deviceUpdateRepository.save(du);
        }

        return schedule;
    }
}