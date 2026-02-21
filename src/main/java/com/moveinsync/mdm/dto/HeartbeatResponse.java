package com.moveinsync.mdm.dto;

import lombok.Data;

@Data
public class HeartbeatResponse {

    private String message;
    private Boolean upgradeRequired;
    private String latestVersion;
    private Boolean mandatory;
}