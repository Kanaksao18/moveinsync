package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.VersionCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VersionCompatibilityRepository
        extends JpaRepository<VersionCompatibility, Long>, JpaSpecificationExecutor<VersionCompatibility> {

    Optional<VersionCompatibility> findByFromVersionAndToVersion(
            String fromVersion,
            String toVersion
    );
}
