package com.moveinsync.mdm.service;

import com.moveinsync.mdm.entity.VersionCompatibility;
import com.moveinsync.mdm.exception.BadRequestException;
import com.moveinsync.mdm.repository.VersionCompatibilityRepository;
import lombok.RequiredArgsConstructor;
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
}