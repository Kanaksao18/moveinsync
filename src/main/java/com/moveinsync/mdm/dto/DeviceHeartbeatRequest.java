package com.moveinsync.mdm.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class DeviceHeartbeatRequest {

    @NotBlank
    private String imei;
    private String appVersion;
    private String os;
    private String model;
    private String region;
}