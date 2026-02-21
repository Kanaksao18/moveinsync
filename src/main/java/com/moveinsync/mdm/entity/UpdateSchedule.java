package com.moveinsync.mdm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "update_schedules")
@Getter
@Setter
public class UpdateSchedule extends BaseEntity {

    private String fromVersion;
    private String toVersion;

    private String region;   // Bangalore, Chennai etc

    private LocalDateTime scheduledTime;

    private String rolloutType; // IMMEDIATE / PHASED
    private String rollbackScope = "FAILED_ONLY";

    private Integer percentage; // used for phased rollout

    private Integer batchPercentage;
    private Integer batchNumber;

    private Integer failureThreshold;  // %allowed failure
    private  String status;            // ACTIVE , Failed , COMPLETED, ROLLED_BACK
}