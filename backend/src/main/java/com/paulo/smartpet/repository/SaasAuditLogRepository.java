package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.SaasAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaasAuditLogRepository extends JpaRepository<SaasAuditLog, Long> {

    List<SaasAuditLog> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<SaasAuditLog> findAllByOrderByCreatedAtDesc();
}