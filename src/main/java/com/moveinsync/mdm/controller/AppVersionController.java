package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.AppVersionRequest;
import com.moveinsync.mdm.entity.AppVersion;
import com.moveinsync.mdm.service.AppVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    public Page<AppVersion> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean mandatory,
            @RequestParam(required = false) String customizationTag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return service.getAllVersions(search, mandatory, customizationTag, pageable);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 200);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
