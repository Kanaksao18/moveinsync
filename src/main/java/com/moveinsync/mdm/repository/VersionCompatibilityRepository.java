package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.VersionCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VersionCompatibilityRepository
        extends JpaRepository<VersionCompatibility, Long> {

    Optional<VersionCompatibility> findByFromVersionAndToVersion(
            String fromVersion,
            String toVersion
    );
}