package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.SaasWebhookEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaasWebhookEventLogRepository extends JpaRepository<SaasWebhookEventLog, Long> {

    boolean existsByEventKey(String eventKey);
}