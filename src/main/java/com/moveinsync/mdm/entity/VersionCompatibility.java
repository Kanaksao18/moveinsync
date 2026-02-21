package com.moveinsync.mdm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "version_compatibility")
@Getter
@Setter
public class VersionCompatibility extends BaseEntity {

    private String fromVersion;

    private String toVersion;

    // If true → device MUST upgrade to intermediateVersion first
    private Boolean requiresIntermediate;

    // Example: "2.0"
    private String intermediateVersion;

    // Optional description
    private String notes;
}