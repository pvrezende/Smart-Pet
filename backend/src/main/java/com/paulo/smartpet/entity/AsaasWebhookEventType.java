package com.paulo.smartpet.entity;

public enum AsaasWebhookEventType {
    PAYMENT_CREATED,
    PAYMENT_UPDATED,
    PAYMENT_CONFIRMED,
    PAYMENT_RECEIVED,
    PAYMENT_OVERDUE,
    PAYMENT_DELETED,
    PAYMENT_RESTORED,
    PAYMENT_REFUNDED,
    PAYMENT_CHARGEBACK_REQUESTED,
    PAYMENT_CHARGEBACK_DISPUTE,
    PAYMENT_AWAITING_CHARGEBACK_REVERSAL,
    PAYMENT_DUNNING_RECEIVED,
    UNKNOWN;

    public static AsaasWebhookEventType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        try {
            return AsaasWebhookEventType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}