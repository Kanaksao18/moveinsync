package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.entity.VersionCompatibility;
import com.moveinsync.mdm.service.VersionCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    public Page<VersionCompatibility> list(
            @RequestParam(required = false) String fromVersion,
            @RequestParam(required = false) String toVersion,
            @RequestParam(required = false) Boolean requiresIntermediate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return service.getAllRules(fromVersion, toVersion, requiresIntermediate, pageable);
    }

    @PutMapping("/{id}")
    public VersionCompatibility update(@PathVariable Long id, @RequestBody VersionCompatibility vc) {
        return service.updateRule(id, vc);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteRule(id);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 200);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
