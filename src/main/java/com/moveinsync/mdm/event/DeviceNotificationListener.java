package com.moveinsync.mdm.event;

import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.entity.DeviceUpdate;
import com.moveinsync.mdm.repository.AuditLogRepository;
import com.moveinsync.mdm.repository.DeviceUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceNotificationListener {

    private final DeviceUpdateRepository deviceUpdateRepository;
    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeviceNotification(DeviceNotificationEvent event) {
        DeviceUpdate update = deviceUpdateRepository.findById(event.getDeviceUpdateId()).orElse(null);
        if (update == null) {
            return;
        }

        update.setState("DEVICE_NOTIFIED");
        update.setNotifiedAt(LocalDateTime.now());
        deviceUpdateRepository.save(update);

        AuditLog logEntry = new AuditLog();
        logEntry.setActor("SYSTEM");
        logEntry.setAction("DEVICE_NOTIFIED");
        logEntry.setDetails("Asynchronous notification dispatched");
        logEntry.setScheduleId(update.getSchedule() != null ? update.getSchedule().getId() : null);
        logEntry.setDeviceId(update.getDevice() != null ? update.getDevice().getId() : null);
        logEntry.setDeviceImei(update.getDevice() != null ? update.getDevice().getImei() : null);
        auditLogRepository.save(logEntry);

        log.debug("Notification event processed for deviceUpdate={}", update.getId());
    }
}
