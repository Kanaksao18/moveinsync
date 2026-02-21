package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

    Optional<AppVersion> findByVersionCode(String versionCode);
    Optional<AppVersion> findTopByOrderByReleaseDateDesc();
}