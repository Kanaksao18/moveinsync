package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.AppVersionRequest;
import com.moveinsync.mdm.entity.AppVersion;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionRepository repository;

    /**
     * Cached latest version (high-read, low-write)
     */
    @Cacheable(value = "latestVersion")
    public AppVersion getLatestVersion() {
        return repository.findTopByOrderByReleaseDateDesc()
                .orElse(null);
    }

    /**
     * Evict cache whenever a new version is released
     */
    @CacheEvict(value = "latestVersion", allEntries = true)
    public AppVersion createVersion(AppVersionRequest request) {

        repository.findByVersionCode(request.getVersionCode())
                .ifPresent(v -> {
                    throw new BadRequestException("Version already exists");
                });

        AppVersion version = new AppVersion();

        version.setVersionCode(request.getVersionCode());
        version.setVersionName(request.getVersionName());
        version.setMandatory(request.getMandatory());
        version.setSupportedOs(request.getSupportedOs());
        version.setCustomizationTag(request.getCustomizationTag());
        version.setReleaseDate(LocalDate.now());

        return repository.save(version);
    }

    /**
     * Prevent downgrade
     */
    public boolean isDowngrade(String current, String target) {
        return Double.parseDouble(target) < Double.parseDouble(current);
    }
}