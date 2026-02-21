package com.moveinsync.mdm.dto;

import lombok.Data;

@Data
public class UpdateScheduleRequest {

    private String fromVersion;
    private String toVersion;
    private String region;
    private String rolloutType;
    private Integer percentage;


}