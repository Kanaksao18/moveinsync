package com.moveinsync.mdm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
public class Device extends BaseEntity {

    private String imei;          // primary device identifier

    private String appVersion;

    private String os;

    private String model;

    private String region;

    private LocalDateTime lastOpenTime;

    private Boolean active = true;
}