package com.moveinsync.mdm.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeviceNotificationEvent {
    private final Long deviceUpdateId;
}
