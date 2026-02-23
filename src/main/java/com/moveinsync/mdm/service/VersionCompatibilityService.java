package com.moveinsync.mdm.service;

import com.moveinsync.mdm.entity.VersionCompatibility;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.exception.ResourceNotFoundException;
import com.moveinsync.mdm.repository.VersionCompatibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VersionCompatibilityService {

    private final VersionCompatibilityRepository repository;

    /**
     * Validate upgrade path
     */
    public void validateUpgrade(String current, String target) {

        VersionCompatibility rule = repository
                .findByFromVersionAndToVersion(current, target)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Upgrade path not allowed from "
                                        + current + " to " + target));

        if (Boolean.TRUE.equals(rule.getRequiresIntermediate())) {

            throw new BadRequestException(
                    "Intermediate upgrade required: "
                            + rule.getIntermediateVersion());
        }
    }

    /**
     * Admin creates compatibility rule
     */
    public VersionCompatibility createRule(VersionCompatibility vc) {
        return repository.save(vc);
    }

    public Page<VersionCompatibility> getAllRules(
            String fromVersion,
            String toVersion,
            Boolean requiresIntermediate,
            Pageable pageable
    ) {
        Specification<VersionCompatibility> spec =
                Specification.<VersionCompatibility>where((Specification<VersionCompatibility>) null);

        if (fromVersion != null && !fromVersion.isBlank()) {
            String query = "%" + fromVersion.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("fromVersion")), query));
        }

        if (toVersion != null && !toVersion.isBlank()) {
            String query = "%" + toVersion.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("toVersion")), query));
        }

        if (requiresIntermediate != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("requiresIntermediate"), requiresIntermediate));
        }

        return repository.findAll(spec, pageable);
    }

    public VersionCompatibility updateRule(Long id, VersionCompatibility request) {
        VersionCompatibility existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compatibility rule not found"));

        existing.setFromVersion(request.getFromVersion());
        existing.setToVersion(request.getToVersion());
        existing.setRequiresIntermediate(request.getRequiresIntermediate());
        existing.setIntermediateVersion(request.getIntermediateVersion());
        existing.setNotes(request.getNotes());

        return repository.save(existing);
    }

    public void deleteRule(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Compatibility rule not found");
        }
        repository.deleteById(id);
    }
}
