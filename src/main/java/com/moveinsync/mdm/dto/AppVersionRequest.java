package com.moveinsync.mdm.dto;

import lombok.Data;

@Data
public class AppVersionRequest {

    private String versionCode;
    private String versionName;
    private Boolean mandatory;
    private String supportedOs;
    private String customizationTag;
}