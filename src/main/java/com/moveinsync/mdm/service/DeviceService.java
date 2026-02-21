package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.DeviceHeartbeatRequest;
import com.moveinsync.mdm.dto.HeartbeatResponse;
import com.moveinsync.mdm.entity.AppVersion;
import com.moveinsync.mdm.entity.Device;
import com.moveinsync.mdm.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final AppVersionService appVersionService;

    /**
     * Heartbeat API
     * Evicts dashboard cache + uses cached latest version
     */
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
        device.setLastOpenTime(LocalDateTime.now());
        device.setActive(true);

        deviceRepository.save(device);

        HeartbeatResponse response = new HeartbeatResponse();

        // 🔥 Cached call (Redis)
        AppVersion latest = appVersionService.getLatestVersion();

        if (latest == null) {
            response.setMessage("No versions configured");
            response.setUpgradeRequired(false);
            return response;
        }

        double deviceVersion = Double.parseDouble(request.getAppVersion());
        double latestVersion = Double.parseDouble(latest.getVersionCode());

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
}