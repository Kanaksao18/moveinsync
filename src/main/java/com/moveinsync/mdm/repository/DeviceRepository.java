package com.moveinsync.mdm.repository;
import com.moveinsync.mdm.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

    Optional<Device> findByImei(String imei);
    long countByActiveTrue();
    long countByLastOpenTimeBefore(LocalDateTime threshold);
    List<Device> findByLastOpenTimeBefore(LocalDateTime threshold);
}
