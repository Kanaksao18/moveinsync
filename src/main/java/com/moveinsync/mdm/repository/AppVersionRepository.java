package com.moveinsync.mdm.repository;

import com.moveinsync.mdm.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long>, JpaSpecificationExecutor<AppVersion> {

    Optional<AppVersion> findByVersionCode(String versionCode);
    Optional<AppVersion> findTopByOrderByReleaseDateDesc();
    List<AppVersion> findAllByOrderByReleaseDateDesc();
}
