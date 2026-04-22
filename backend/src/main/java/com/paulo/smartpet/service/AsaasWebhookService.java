package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.asaas.AsaasWebhookEvent;
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

        StoreSubscription subscription = storeSubscriptionRepository.findAll().stream()
                .filter(item -> externalBillingId.equals(item.getExternalBillingId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Assinatura não encontrada para o pagamento informado"));

        BillingStatus previousBillingStatus = subscription.getBillingStatus();
        String previousExternalBillingStatus = subscription.getExternalBillingStatus();

        subscription.setExternalBillingStatus(event.payment().status());

        if (isPaidEvent(event.event(), event.payment().status())) {
            subscription.setBillingStatus(BillingStatus.PAID);
        }

        storeSubscriptionRepository.save(subscription);

        saveBillingHistory(subscription, previousBillingStatus, previousExternalBillingStatus, event);

        return "Evento " + event.event() + " processado para cobrança " + externalBillingId;
    }

    private boolean isPaidEvent(String eventName, String paymentStatus) {
        if (eventName != null) {
            String normalizedEvent = eventName.trim().toUpperCase();
            if ("PAYMENT_RECEIVED".equals(normalizedEvent)
                    || "PAYMENT_CONFIRMED".equals(normalizedEvent)
                    || "PAYMENT_UPDATED".equals(normalizedEvent)) {
                return paymentStatus != null && isPaidStatus(paymentStatus);
            }
        }

        return paymentStatus != null && isPaidStatus(paymentStatus);
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
            AsaasWebhookEvent event
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
        history.setNotes(buildNotes(event.event(), previousExternalBillingStatus, subscription.getExternalBillingStatus()));
        storeSubscriptionBillingHistoryRepository.save(history);
    }

    private String buildNotes(String eventName, String previousExternalBillingStatus, String newExternalBillingStatus) {
        String notes = "Webhook ASAAS processado. event=" + safe(eventName)
                + ", previousExternalBillingStatus=" + safe(previousExternalBillingStatus)
                + ", newExternalBillingStatus=" + safe(newExternalBillingStatus);

        return notes.length() > 255 ? notes.substring(0, 255) : notes;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}