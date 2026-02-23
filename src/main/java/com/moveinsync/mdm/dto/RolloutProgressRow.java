package com.moveinsync.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RolloutProgressRow {
    private Long scheduleId;
    private String fromVersion;
    private String toVersion;
    private String region;
    private String status;
    private long totalDevices;
    private long completedDevices;
    private long failedDevices;
    private int successRate;
    private int failureRate;
}
