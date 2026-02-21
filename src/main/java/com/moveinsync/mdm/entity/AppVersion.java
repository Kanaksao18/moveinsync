package com.moveinsync.mdm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "app_versions")
@Getter
@Setter
public class AppVersion extends BaseEntity {

    private String versionCode;     // eg: 4.2
    private String versionName;     // eg: Release_4_2

    private LocalDate releaseDate;

    private Boolean mandatory = false;

    private String supportedOs;     // Android >=12

    private String customizationTag; // region/client

}