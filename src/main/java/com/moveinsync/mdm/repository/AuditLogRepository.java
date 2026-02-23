package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    List<AuditLog> findByDeviceIdOrderByTimestampAsc(Long deviceId);
    List<AuditLog> findByScheduleIdOrderByTimestampAsc(Long scheduleId);
}
