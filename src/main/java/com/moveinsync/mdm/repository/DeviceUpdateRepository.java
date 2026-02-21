package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.DeviceUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceUpdateRepository extends JpaRepository<DeviceUpdate, Long> {
}