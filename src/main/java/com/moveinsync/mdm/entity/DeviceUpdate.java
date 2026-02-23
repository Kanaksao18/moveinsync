package com.moveinsync.mdm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_updates")
@Getter
@Setter
public class DeviceUpdate extends BaseEntity {

    @ManyToOne
    private Device device;

    @ManyToOne
    private UpdateSchedule schedule;

    private String state;
    // SCHEDULED
    // DOWNLOAD_STARTED
    // DOWNLOAD_COMPLETED
    // INSTALL_STARTED
    // COMPLETED
    // FAILED

    private LocalDateTime notifiedAt;
    private LocalDateTime downloadStartedAt;
    private LocalDateTime downloadCompletedAt;
    private LocalDateTime installStartedAt;
    private LocalDateTime installCompletedAt;
    private LocalDateTime failedAt;
    private LocalDateTime nextRetryAt;

    private Integer retryCount = 0;
    private String failureStage;
    private String failureReason;
}
