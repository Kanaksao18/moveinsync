package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.DashboardSummaryResponse;
import com.moveinsync.mdm.dto.HeartbeatResponse;
import com.moveinsync.mdm.dto.DeviceHeartbeatRequest;
import com.moveinsync.mdm.entity.AppVersion;
import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.repository.AuditLogRepository;
import com.moveinsync.mdm.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final AppVersionService appVersionService;
    private final UpdateScheduleService updateScheduleService;
    private final AuditLogRepository auditLogRepository;

    @CacheEvict(value = "deviceCount", allEntries = true)
    public HeartbeatResponse heartbeat(DeviceHeartbeatRequest request) {
        log.info("Heartbeat from device {}", request.getImei());

        Device device = deviceRepository.findByImei(request.getImei())
                .orElseGet(Device::new);

        device.setImei(request.getImei());
        device.setAppVersion(request.getAppVersion());
        device.setOs(request.getOs());
        device.setModel(request.getModel());
        device.setRegion(request.getRegion());
        device.setCustomizationTag(request.getCustomizationTag());
        device.setDeviceGroup(request.getDeviceGroup());
        device.setLastOpenTime(LocalDateTime.now());
        device.setActive(true);
        deviceRepository.save(device);

        HeartbeatResponse response = new HeartbeatResponse();
        // Latest version is Redis-cached to keep heartbeat path fast.
        AppVersion latest = appVersionService.getLatestVersion();

        if (latest == null) {
            response.setMessage("No versions configured");
            response.setUpgradeRequired(false);
            return response;
        }

        double deviceVersion = parseVersion(request.getAppVersion());
        double latestVersion = parseVersion(latest.getVersionCode());

        if (deviceVersion < latestVersion) {
            response.setUpgradeRequired(true);
            response.setLatestVersion(latest.getVersionCode());
            response.setMandatory(latest.getMandatory());
            response.setMessage("Upgrade required");
        } else {
            response.setUpgradeRequired(false);
            response.setMessage("Device compliant");
        }

        return response;
    }

    public Page<Device> getDevices(String search, String region, Boolean active, String appVersion, Pageable pageable) {
        Specification<Device> spec = Specification.<Device>where((Specification<Device>) null);

        if (search != null && !search.isBlank()) {
            String query = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("imei")), query),
                    cb.like(cb.lower(root.get("model")), query),
                    cb.like(cb.lower(root.get("os")), query),
                    cb.like(cb.lower(root.get("region")), query),
                    cb.like(cb.lower(root.get("appVersion")), query)
            ));
        }

        if (region != null && !region.isBlank()) {
            String value = region.trim().toLowerCase();
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("region")), value));
        }

        if (active != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("active"), active));
        }

        if (appVersion != null && !appVersion.isBlank()) {
            String value = appVersion.trim().toLowerCase();
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("appVersion")), value));
        }

        return deviceRepository.findAll(spec, pageable);
    }

    public Device addOrUpdateDevice(DeviceHeartbeatRequest request) {
        if (request.getImei() == null || request.getImei().isBlank()) {
            throw new BadRequestException("IMEI is required");
        }

        Device device = deviceRepository.findByImei(request.getImei())
                .orElseGet(Device::new);

        device.setImei(request.getImei());
        device.setAppVersion(request.getAppVersion());
        device.setOs(request.getOs());
        device.setModel(request.getModel());
        device.setRegion(request.getRegion());
        device.setCustomizationTag(request.getCustomizationTag());
        device.setDeviceGroup(request.getDeviceGroup());
        device.setLastOpenTime(LocalDateTime.now());
        if (device.getActive() == null) {
            device.setActive(true);
        }
        return deviceRepository.save(device);
    }

    public DashboardSummaryResponse getDashboardSummary() {
        // Aggregations are currently in-memory; can be pushed to SQL for large-scale deployments.
        List<Device> devices = deviceRepository.findAll();
        long total = devices.size();
        long active = devices.stream().filter(d -> Boolean.TRUE.equals(d.getActive())).count();
        long inactive = devices.stream().filter(d -> !Boolean.TRUE.equals(d.getActive())).count();

        Map<String, Long> versionDistribution = devices.stream()
                .collect(Collectors.groupingBy(d -> d.getAppVersion() != null ? d.getAppVersion() : "unknown", Collectors.counting()));

        Map<String, Long> regionDistribution = devices.stream()
                .collect(Collectors.groupingBy(d -> d.getRegion() != null ? d.getRegion() : "unknown", Collectors.counting()));

        Map<String, Map<String, Long>> heatmap = devices.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getRegion() != null ? d.getRegion() : "unknown",
                        Collectors.groupingBy(d -> d.getAppVersion() != null ? d.getAppVersion() : "unknown", Collectors.counting())
                ));

        return new DashboardSummaryResponse(
                total,
                active,
                inactive,
                versionDistribution,
                regionDistribution,
                heatmap,
                updateScheduleService.getRolloutProgress()
        );
    }

    public List<Device> getInactiveDevices(int inactiveHours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(inactiveHours);
        return deviceRepository.findByLastOpenTimeBefore(threshold);
    }

    public List<AuditLog> getDeviceTimeline(Long deviceId) {
        return auditLogRepository.findByDeviceIdOrderByTimestampAsc(deviceId);
    }

    private double parseVersion(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            throw new BadRequestException("Version must be numeric");
        }
    }
}
