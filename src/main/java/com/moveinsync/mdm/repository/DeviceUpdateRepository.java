package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.entity.UpdateSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceUpdateRepository extends JpaRepository<DeviceUpdate, Long>, JpaSpecificationExecutor<DeviceUpdate> {
    Optional<DeviceUpdate> findByDeviceAndSchedule(Device device, UpdateSchedule schedule);
    long countBySchedule(UpdateSchedule schedule);

    long countByScheduleAndState(UpdateSchedule schedule, String state);

    List<DeviceUpdate> findByScheduleAndState(UpdateSchedule schedule, String state);
    List<DeviceUpdate> findBySchedule(UpdateSchedule schedule);
    List<DeviceUpdate> findByScheduleId(Long scheduleId);
    long countByScheduleAndStateIn(UpdateSchedule schedule, List<String> states);
    List<DeviceUpdate> findByStateAndNextRetryAtLessThanEqual(String state, LocalDateTime now);
}
