package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.asaas.AsaasWebhookEvent;
import com.paulo.smartpet.entity.AsaasWebhookEventType;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.StoreSubscriptionBillingHistory;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.repository.StoreSubscriptionBillingHistoryRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsaasWebhookService {

    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository;

    public AsaasWebhookService(
            StoreSubscriptionRepository storeSubscriptionRepository,
            StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository
    ) {
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.storeSubscriptionBillingHistoryRepository = storeSubscriptionBillingHistoryRepository;
    }

    @Transactional
    public String process(AsaasWebhookEvent event) {
        if (event == null) {
            throw new BusinessException("Payload do webhook não informado");
        }

        if (event.payment() == null) {
            throw new BusinessException("Pagamento do webhook não informado");
        }

        String externalBillingId = event.payment().id();
        if (externalBillingId == null || externalBillingId.isBlank()) {
            throw new BusinessException("Id externo do pagamento não informado no webhook");
        }

        AsaasWebhookEventType eventType = AsaasWebhookEventType.fromValue(event.event());

        StoreSubscription subscription = storeSubscriptionRepository.findAll().stream()
                .filter(item -> externalBillingId.equals(item.getExternalBillingId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Assinatura não encontrada para o pagamento informado"));

        BillingStatus previousBillingStatus = subscription.getBillingStatus();
        String previousExternalBillingStatus = subscription.getExternalBillingStatus();

        subscription.setExternalBillingStatus(event.payment().status());
        applyInternalBillingStatus(subscription, eventType, event.payment().status());

        storeSubscriptionRepository.save(subscription);

        saveBillingHistory(subscription, previousBillingStatus, previousExternalBillingStatus, eventType);

        return "Evento " + eventType.name() + " processado para cobrança " + externalBillingId;
    }

    private void applyInternalBillingStatus(
            StoreSubscription subscription,
            AsaasWebhookEventType eventType,
            String paymentStatus
    ) {
        if (isPaidEvent(eventType, paymentStatus)) {
            subscription.setBillingStatus(BillingStatus.PAID);
            return;
        }

        if (isOverdueEvent(eventType, paymentStatus)) {
            subscription.setBillingStatus(BillingStatus.OVERDUE);
            return;
        }

        if (isPendingEvent(eventType, paymentStatus)) {
            subscription.setBillingStatus(BillingStatus.PENDING);
        }
    }

    private boolean isPaidEvent(AsaasWebhookEventType eventType, String paymentStatus) {
        if (eventType == AsaasWebhookEventType.PAYMENT_RECEIVED
                || eventType == AsaasWebhookEventType.PAYMENT_CONFIRMED
                || eventType == AsaasWebhookEventType.PAYMENT_DUNNING_RECEIVED
                || eventType == AsaasWebhookEventType.PAYMENT_UPDATED) {
            return paymentStatus != null && isPaidStatus(paymentStatus);
        }

        return paymentStatus != null && isPaidStatus(paymentStatus);
    }

    private boolean isOverdueEvent(AsaasWebhookEventType eventType, String paymentStatus) {
        if (eventType == AsaasWebhookEventType.PAYMENT_OVERDUE) {
            return true;
        }

        if (paymentStatus == null || paymentStatus.isBlank()) {
            return false;
        }

        String normalizedStatus = paymentStatus.trim().toUpperCase();
        return "OVERDUE".equals(normalizedStatus);
    }

    private boolean isPendingEvent(AsaasWebhookEventType eventType, String paymentStatus) {
        if (eventType == AsaasWebhookEventType.PAYMENT_DELETED
                || eventType == AsaasWebhookEventType.PAYMENT_RESTORED
                || eventType == AsaasWebhookEventType.PAYMENT_REFUNDED
                || eventType == AsaasWebhookEventType.PAYMENT_CHARGEBACK_REQUESTED
                || eventType == AsaasWebhookEventType.PAYMENT_CHARGEBACK_DISPUTE
                || eventType == AsaasWebhookEventType.PAYMENT_AWAITING_CHARGEBACK_REVERSAL) {
            return true;
        }

        if (paymentStatus == null || paymentStatus.isBlank()) {
            return false;
        }

        String normalizedStatus = paymentStatus.trim().toUpperCase();
        return "PENDING".equals(normalizedStatus)
                || "AWAITING_RISK_ANALYSIS".equals(normalizedStatus)
                || "REFUNDED".equals(normalizedStatus)
                || "RECEIVED_IN_CASH_UNDONE".equals(normalizedStatus)
                || "CHARGEBACK_REQUESTED".equals(normalizedStatus)
                || "CHARGEBACK_DISPUTE".equals(normalizedStatus)
                || "AWAITING_CHARGEBACK_REVERSAL".equals(normalizedStatus);
    }

    private boolean isPaidStatus(String paymentStatus) {
        String normalizedStatus = paymentStatus.trim().toUpperCase();
        return "RECEIVED".equals(normalizedStatus)
                || "CONFIRMED".equals(normalizedStatus)
                || "RECEIVED_IN_CASH".equals(normalizedStatus);
    }

    private void saveBillingHistory(
            StoreSubscription subscription,
            BillingStatus previousBillingStatus,
            String previousExternalBillingStatus,
            AsaasWebhookEventType eventType
    ) {
        StoreSubscriptionBillingHistory history = new StoreSubscriptionBillingHistory();
        history.setStore(subscription.getStore());
        history.setPreviousBillingStatus(previousBillingStatus);
        history.setNewBillingStatus(subscription.getBillingStatus());
        history.setPreviousMonthlyPrice(subscription.getMonthlyPrice());
        history.setNewMonthlyPrice(subscription.getMonthlyPrice());
        history.setPreviousBillingDay(subscription.getBillingDay());
        history.setNewBillingDay(subscription.getBillingDay());
        history.setPreviousNextBillingDate(subscription.getNextBillingDate());
        history.setNewNextBillingDate(subscription.getNextBillingDate());
        history.setPaymentProvider(subscription.getPaymentProvider());
        history.setExternalCustomerId(subscription.getExternalCustomerId());
        history.setExternalSubscriptionId(subscription.getExternalSubscriptionId());
        history.setExternalBillingId(subscription.getExternalBillingId());
        history.setExternalBillingStatus(subscription.getExternalBillingStatus());
        history.setNotes(buildNotes(eventType, previousExternalBillingStatus, subscription.getExternalBillingStatus()));
        storeSubscriptionBillingHistoryRepository.save(history);
    }

    private String buildNotes(
            AsaasWebhookEventType eventType,
            String previousExternalBillingStatus,
            String newExternalBillingStatus
    ) {
        String notes = "Webhook ASAAS processado. event=" + safe(eventType.name())
                + ", previousExternalBillingStatus=" + safe(previousExternalBillingStatus)
                + ", newExternalBillingStatus=" + safe(newExternalBillingStatus);

        return notes.length() > 255 ? notes.substring(0, 255) : notes;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}