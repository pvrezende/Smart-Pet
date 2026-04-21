package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.SaasAuditLog;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.repository.SaasAuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SaasAuditService {

    private final SaasAuditLogRepository repository;

    public SaasAuditService(SaasAuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(User user, Long storeId, String action, String details) {
        SaasAuditLog log = new SaasAuditLog(
                storeId,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                action,
                details
        );

        repository.save(log);
    }
}