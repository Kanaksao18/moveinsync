package com.moveinsync.mdm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateScheduleRequest {

    private String fromVersion;
    private String toVersion;
    private String region;
    private String customizationTag;
    private String deviceGroup;
    private String rolloutType;
    private Integer percentage;
    private Integer batchPercentage;
    private String rollbackScope;
    private Integer failureThreshold;
    private Integer maxRetries;
    private Integer retryBackoffMinutes;
    private LocalDateTime scheduledTime;

}
