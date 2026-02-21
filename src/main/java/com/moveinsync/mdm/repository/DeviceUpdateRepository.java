package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.entity.UpdateSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceUpdateRepository extends JpaRepository<DeviceUpdate, Long> {
    Optional<DeviceUpdate> findByDeviceAndSchedule(Device device, UpdateSchedule schedule);
    long countBySchedule(UpdateSchedule schedule);

    long countByScheduleAndState(UpdateSchedule schedule, String state);

    List<DeviceUpdate> findByScheduleAndState(UpdateSchedule schedule, String state);
    List<DeviceUpdate> findBySchedule(UpdateSchedule schedule);
}
