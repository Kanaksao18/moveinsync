package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.UpdateStateRequest;
import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.exception.ResourceNotFoundException;
import com.moveinsync.mdm.repository.AuditLogRepository;
import com.moveinsync.mdm.repository.DeviceUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceUpdateService {

    private final DeviceUpdateRepository deviceUpdateRepository;
    private final AuditLogRepository auditLogRepository;

    public void updateState(UpdateStateRequest request) {

        DeviceUpdate du = deviceUpdateRepository.findById(request.getDeviceUpdateId())
                .orElseThrow(() -> new ResourceNotFoundException("Device update not found"));

        du.setState(request.getState());
        du.setFailureReason(request.getFailureReason());

        deviceUpdateRepository.save(du);

        AuditLog log = new AuditLog();
        log.setActor("DEVICE");
        log.setAction(request.getState());
        log.setDetails("DeviceUpdateId: " + du.getId());

        auditLogRepository.save(log);
    }
}