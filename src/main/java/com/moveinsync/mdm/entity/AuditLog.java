package com.moveinsync.mdm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog extends BaseEntity {

    private String actor;      // admin / system / device

    private String action;     // UPDATE_SCHEDULED, DOWNLOAD_STARTED etc

    private String details;

    private LocalDateTime timestamp = LocalDateTime.now();
}