package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasBillingAutomationResponse;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaasBillingAutomationService {

    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final StoreSubscriptionPaymentService storeSubscriptionPaymentService;

    public SaasBillingAutomationService(
            StoreSubscriptionRepository storeSubscriptionRepository,
            StoreSubscriptionPaymentService storeSubscriptionPaymentService
    ) {
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.storeSubscriptionPaymentService = storeSubscriptionPaymentService;
    }

    @Transactional
    public SaasBillingAutomationResponse executeAutomaticChargeGeneration() {
        List<StoreSubscription> subscriptions = storeSubscriptionRepository.findAllByOrderByCreatedAtDesc();

        long totalChecked = subscriptions.size();
        long totalGenerated = 0L;
        long totalSkipped = 0L;

        LocalDate today = LocalDate.now();

        for (StoreSubscription subscription : subscriptions) {
            if (!isEligibleForAutomaticCharge(subscription, today)) {
                totalSkipped++;
                continue;
            }

            try {
                storeSubscriptionPaymentService.generatePaymentLink(
                        subscription.getStore().getId(),
                        "PIX"
                );
                totalGenerated++;
            } catch (Exception ex) {
                totalSkipped++;
            }
        }

        return new SaasBillingAutomationResponse(
                totalChecked,
                totalGenerated,
                totalSkipped,
                LocalDateTime.now()
        );
    }

    private boolean isEligibleForAutomaticCharge(StoreSubscription subscription, LocalDate today) {
        if (subscription == null || subscription.getStore() == null || subscription.getStore().getId() == null) {
            return false;
        }

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            return false;
        }

        if (subscription.getBillingStatus() == BillingStatus.CANCELED) {
            return false;
        }

        if (subscription.getNextBillingDate() == null) {
            return false;
        }

        return !subscription.getNextBillingDate().isAfter(today);
    }
}