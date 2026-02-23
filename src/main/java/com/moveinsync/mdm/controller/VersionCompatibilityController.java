package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.entity.VersionCompatibility;
import com.moveinsync.mdm.service.VersionCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compatibility")
@RequiredArgsConstructor
public class VersionCompatibilityController {

    private final VersionCompatibilityService service;

    @PostMapping
    public VersionCompatibility create(@RequestBody VersionCompatibility vc) {
        return service.createRule(vc);
    }

    @GetMapping
    public List<VersionCompatibility> list() {
        return service.getAllRules();
    }

    @PutMapping("/{id}")
    public VersionCompatibility update(@PathVariable Long id, @RequestBody VersionCompatibility vc) {
        return service.updateRule(id, vc);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteRule(id);
    }
}
