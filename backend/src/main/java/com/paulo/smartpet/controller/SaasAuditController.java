package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.SaasAuditLogResponse;
import com.paulo.smartpet.entity.SaasAuditLog;
import com.paulo.smartpet.repository.SaasAuditLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saas-audit")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SaasAuditController {

    private final SaasAuditLogRepository repository;

    public SaasAuditController(SaasAuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SaasAuditLogResponse> getAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/store/{storeId}")
    public List<SaasAuditLogResponse> getByStore(@PathVariable Long storeId) {
        return repository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SaasAuditLogResponse toResponse(SaasAuditLog log) {
        return new SaasAuditLogResponse(
                log.getId(),
                log.getStoreId(),
                log.getUserId(),
                log.getUsername(),
                log.getUserRole(),
                log.getAction(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}