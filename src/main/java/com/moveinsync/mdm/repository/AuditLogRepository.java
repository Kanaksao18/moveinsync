package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}