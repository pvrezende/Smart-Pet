package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.asaas.AsaasWebhookEvent;
import com.paulo.smartpet.entity.AsaasWebhookEventType;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.SaasWebhookEventLog;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.StoreSubscriptionBillingHistory;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.repository.SaasWebhookEventLogRepository;
import com.paulo.smartpet.repository.StoreSubscriptionBillingHistoryRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AsaasWebhookService {

    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository;
    private final SaasWebhookEventLogRepository saasWebhookEventLogRepository;
    private final SaasBillingService saasBillingService;

    public AsaasWebhookService(
            StoreSubscriptionRepository storeSubscriptionRepository,
            StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository,
            SaasWebhookEventLogRepository saasWebhookEventLogRepository,
            SaasBillingService saasBillingService
    ) {
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.storeSubscriptionBillingHistoryRepository = storeSubscriptionBillingHistoryRepository;
        this.saasWebhookEventLogRepository = saasWebhookEventLogRepository;
        this.saasBillingService = saasBillingService;
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
        String eventKey = buildEventKey(eventType, externalBillingId, event.payment().status(), event.payment().paymentDate());

        if (saasWebhookEventLogRepository.existsByEventKey(eventKey)) {
            return "Evento " + eventType.name() + " já havia sido processado para cobrança " + externalBillingId;
        }

        StoreSubscription subscription = storeSubscriptionRepository.findAll().stream()
                .filter(item -> externalBillingId.equals(item.getExternalBillingId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Assinatura não encontrada para o pagamento informado"));

        BillingStatus previousBillingStatus = subscription.getBillingStatus();
        String previousExternalBillingStatus = subscription.getExternalBillingStatus();
        LocalDate previousNextBillingDate = subscription.getNextBillingDate();

        subscription.setExternalBillingStatus(event.payment().status());
        applyInternalBillingStatus(subscription, eventType, event.payment().status(), event.payment().paymentDate());

        storeSubscriptionRepository.save(subscription);

        saveBillingHistory(
                subscription,
                previousBillingStatus,
                previousExternalBillingStatus,
                previousNextBillingDate,
                eventType
        );

        saveWebhookEventLog(eventKey, eventType, externalBillingId);

        return "Evento " + eventType.name() + " processado para cobrança " + externalBillingId;
    }

    private String buildEventKey(
            AsaasWebhookEventType eventType,
            String externalBillingId,
            String paymentStatus,
            String paymentDate
    ) {
        return eventType.name()
                + "::" + safe(externalBillingId)
                + "::" + safe(paymentStatus)
                + "::" + safe(paymentDate);
    }

    private void saveWebhookEventLog(String eventKey, AsaasWebhookEventType eventType, String externalBillingId) {
        SaasWebhookEventLog log = new SaasWebhookEventLog();
        log.setEventKey(eventKey);
        log.setEventType(eventType.name());
        log.setExternalBillingId(externalBillingId);
        log.setProcessedAt(LocalDateTime.now());
        saasWebhookEventLogRepository.save(log);
    }

    private void applyInternalBillingStatus(
            StoreSubscription subscription,
            AsaasWebhookEventType eventType,
            String paymentStatus,
            String paymentDate
    ) {
        if (isPaidEvent(eventType, paymentStatus)) {
            subscription.setBillingStatus(BillingStatus.PAID);

            LocalDate paidDate = parsePaymentDate(paymentDate);
            LocalDate nextCycleDate = saasBillingService.calculateNextCycleBillingDate(
                    subscription.getBillingDay(),
                    paidDate
            );

            subscription.setNextBillingDate(nextCycleDate);
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

    private LocalDate parsePaymentDate(String paymentDate) {
        if (paymentDate == null || paymentDate.isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(paymentDate.trim());
        } catch (Exception ex) {
            return LocalDate.now();
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
            LocalDate previousNextBillingDate,
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
        history.setPreviousNextBillingDate(previousNextBillingDate);
        history.setNewNextBillingDate(subscription.getNextBillingDate());
        history.setPaymentProvider(subscription.getPaymentProvider());
        history.setExternalCustomerId(subscription.getExternalCustomerId());
        history.setExternalSubscriptionId(subscription.getExternalSubscriptionId());
        history.setExternalBillingId(subscription.getExternalBillingId());
        history.setExternalBillingStatus(subscription.getExternalBillingStatus());
        history.setNotes(buildNotes(
                eventType,
                previousExternalBillingStatus,
                subscription.getExternalBillingStatus(),
                previousNextBillingDate,
                subscription.getNextBillingDate()
        ));
        storeSubscriptionBillingHistoryRepository.save(history);
    }

    private String buildNotes(
            AsaasWebhookEventType eventType,
            String previousExternalBillingStatus,
            String newExternalBillingStatus,
            LocalDate previousNextBillingDate,
            LocalDate newNextBillingDate
    ) {
        String notes = "Webhook ASAAS processado. event=" + safe(eventType.name())
                + ", previousExternalBillingStatus=" + safe(previousExternalBillingStatus)
                + ", newExternalBillingStatus=" + safe(newExternalBillingStatus)
                + ", previousNextBillingDate=" + safeDate(previousNextBillingDate)
                + ", newNextBillingDate=" + safeDate(newNextBillingDate);

        return notes.length() > 255 ? notes.substring(0, 255) : notes;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String safeDate(LocalDate value) {
        return value == null ? "-" : value.toString();
    }
}