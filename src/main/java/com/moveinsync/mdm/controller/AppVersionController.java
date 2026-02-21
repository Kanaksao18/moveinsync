package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.AppVersionRequest;
import com.moveinsync.mdm.entity.AppVersion;
import com.moveinsync.mdm.service.AppVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionService service;

    /**
     * ADMIN: Create new app version
     */
    @PostMapping
    public AppVersion create(@RequestBody AppVersionRequest request) {
        return service.createVersion(request);
    }

    /**
     * Public: Get latest version (cached)
     */
    @GetMapping("/latest")
    public AppVersion latest() {
        return service.getLatestVersion();
    }
}