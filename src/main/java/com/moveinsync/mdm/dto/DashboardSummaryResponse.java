package com.moveinsync.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalDevices;
    private long activeDevices;
    private long inactiveDevices;
    private Map<String, Long> versionDistribution;
    private Map<String, Long> regionDistribution;
    private Map<String, Map<String, Long>> versionHeatmap;
    private List<RolloutProgressRow> rolloutProgress;
}
