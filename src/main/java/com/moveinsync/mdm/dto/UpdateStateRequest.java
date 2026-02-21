package com.moveinsync.mdm.dto;

import lombok.Data;

@Data
public class UpdateStateRequest {

    private Long deviceUpdateId;
    private String state;
    private String failureReason;
}