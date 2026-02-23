package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByDeviceIdOrderByTimestampAsc(Long deviceId);
    List<AuditLog> findByScheduleIdOrderByTimestampAsc(Long scheduleId);
}
