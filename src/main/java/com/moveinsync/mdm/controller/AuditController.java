package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.entity.AuditLog;
import com.moveinsync.mdm.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public Page<AuditLog> list(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Specification<AuditLog> spec = Specification.<AuditLog>where((Specification<AuditLog>) null);

        if (deviceId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("deviceId"), deviceId));
        }
        if (scheduleId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("scheduleId"), scheduleId));
        }
        if (actor != null && !actor.isBlank()) {
            String value = actor.trim().toLowerCase();
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("actor")), value));
        }
        if (action != null && !action.isBlank()) {
            String query = "%" + action.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("action")), query));
        }

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return auditLogRepository.findAll(spec, pageable);
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 200);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
