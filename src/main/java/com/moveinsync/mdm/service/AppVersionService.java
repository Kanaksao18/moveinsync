package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.AppVersionRequest;
import com.moveinsync.mdm.entity.AppVersion;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.exception.ResourceNotFoundException;
import com.moveinsync.mdm.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionRepository repository;
    private final VersionCompatibilityService compatibilityService;

    /**
     * Cached latest version (high-read, low-write)
     */
    @Cacheable(value = "latestVersion", unless = "#result == null")
    public AppVersion getLatestVersion() {

        return repository.findTopByOrderByReleaseDateDesc()
                .orElseThrow(() ->
                        new ResourceNotFoundException("No app versions found"));
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

        try {
            Double.parseDouble(request.getVersionCode());
        } catch (Exception e) {
            throw new BadRequestException("Version code must be numeric");
        }

        AppVersion version = new AppVersion();

        version.setVersionCode(request.getVersionCode());
        version.setVersionName(request.getVersionName());
        version.setMandatory(request.getMandatory());
        version.setSupportedOs(request.getSupportedOs());
        version.setCustomizationTag(request.getCustomizationTag());
        version.setReleaseDate(LocalDate.now());

        return repository.save(version);
    }

    public List<AppVersion> getAllVersions() {
        return repository.findAllByOrderByReleaseDateDesc();
    }

    /**
     * Validate upgrade using compatibility matrix
     */
    public void validateUpgrade(String current, String target) {
        compatibilityService.validateUpgrade(current, target);
    }
}
