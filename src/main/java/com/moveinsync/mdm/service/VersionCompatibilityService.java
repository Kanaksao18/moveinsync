package com.moveinsync.mdm.service;

import com.moveinsync.mdm.entity.VersionCompatibility;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.exception.ResourceNotFoundException;
import com.moveinsync.mdm.repository.VersionCompatibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<VersionCompatibility> getAllRules() {
        return repository.findAll();
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
