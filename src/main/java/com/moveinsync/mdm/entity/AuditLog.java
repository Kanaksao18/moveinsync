package com.moveinsync.mdm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Immutable
public class AuditLog extends BaseEntity {

    @Column(updatable = false)
    private String actor;      // admin / system / device

    @Column(updatable = false)
    private String action;     // UPDATE_SCHEDULED, DOWNLOAD_STARTED etc

    @Column(updatable = false)
    private String details;
    @Column(updatable = false)
    private Long scheduleId;
    @Column(updatable = false)
    private Long deviceId;
    @Column(updatable = false)
    private String deviceImei;

    @Column(updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @PreUpdate
    public void preventUpdates() {
        throw new IllegalStateException("Audit logs are immutable");
    }
}
