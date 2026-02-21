package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.entity.VersionCompatibility;
import com.moveinsync.mdm.service.VersionCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compatibility")
@RequiredArgsConstructor
public class VersionCompatibilityController {

    private final VersionCompatibilityService service;

    @PostMapping
    public VersionCompatibility create(@RequestBody VersionCompatibility vc) {
        return service.createRule(vc);
    }
}