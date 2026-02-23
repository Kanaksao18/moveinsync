package com.moveinsync.mdm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceUpdateMonitoringRow {
    private Long id;
    private String device;
    private Long deviceId;
    private String currentState;
    private String failureReason;
}
