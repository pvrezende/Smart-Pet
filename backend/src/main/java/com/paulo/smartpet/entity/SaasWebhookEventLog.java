package com.paulo.smartpet.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "saas_webhook_event_log",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_saas_webhook_event_log_event_key", columnNames = "event_key")
        }
)
public class SaasWebhookEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_key", nullable = false, length = 255)
    private String eventKey;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "external_billing_id", nullable = false, length = 100)
    private String externalBillingId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public SaasWebhookEventLog() {
    }

    public Long getId() {
        return id;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getExternalBillingId() {
        return externalBillingId;
    }

    public void setExternalBillingId(String externalBillingId) {
        this.externalBillingId = externalBillingId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}