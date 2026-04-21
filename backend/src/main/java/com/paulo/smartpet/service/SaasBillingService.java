package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.StoreBillingSummaryResponse;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SaasBillingService {

    public BigDecimal getMonthlyPrice(SubscriptionPlan plan) {
        if (plan == null) {
            return BigDecimal.ZERO;
        }

        return switch (plan) {
            case BASIC -> new BigDecimal("99.90");
            case PRO -> new BigDecimal("199.90");
            case ENTERPRISE -> new BigDecimal("499.90");
        };
    }

    public LocalDate calculateNextBillingDate(Integer billingDay, LocalDate referenceDate) {
        int safeBillingDay = billingDay == null ? 10 : billingDay;
        LocalDate base = referenceDate == null ? LocalDate.now() : referenceDate;

        int day = Math.min(safeBillingDay, base.lengthOfMonth());
        LocalDate currentMonthDate = base.withDayOfMonth(day);

        if (!base.isAfter(currentMonthDate)) {
            return currentMonthDate;
        }

        LocalDate nextMonth = base.plusMonths(1);
        int nextDay = Math.min(safeBillingDay, nextMonth.lengthOfMonth());
        return nextMonth.withDayOfMonth(nextDay);
    }

    public boolean isOverdue(BillingStatus billingStatus, LocalDate nextBillingDate) {
        return billingStatus == BillingStatus.OVERDUE
                || (nextBillingDate != null && nextBillingDate.isBefore(LocalDate.now()));
    }

    public StoreBillingSummaryResponse toBillingSummary(StoreSubscription subscription) {
        boolean overdue = isOverdue(subscription.getBillingStatus(), subscription.getNextBillingDate());

        return new StoreBillingSummaryResponse(
                subscription.getStore().getId(),
                subscription.getStore().getName(),
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getBillingStatus(),
                subscription.getMonthlyPrice(),
                subscription.getBillingDay(),
                subscription.getNextBillingDate(),
                overdue
        );
    }

    public BillingStatus resolveInitialBillingStatus(SubscriptionStatus subscriptionStatus) {
        if (subscriptionStatus == SubscriptionStatus.TRIAL) {
            return BillingStatus.TRIAL;
        }

        if (subscriptionStatus == SubscriptionStatus.CANCELED) {
            return BillingStatus.CANCELED;
        }

        return BillingStatus.PENDING;
    }

    public BillingStatus resolveAutomaticBillingStatus(StoreSubscription subscription) {
        if (subscription == null) {
            return null;
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELED
                || subscription.getBillingStatus() == BillingStatus.CANCELED) {
            return BillingStatus.CANCELED;
        }

        if (isTrialActive(subscription)) {
            return BillingStatus.TRIAL;
        }

        if (isOverdue(subscription.getBillingStatus(), subscription.getNextBillingDate())) {
            return BillingStatus.OVERDUE;
        }

        if (subscription.getBillingStatus() == null || subscription.getBillingStatus() == BillingStatus.TRIAL) {
            return BillingStatus.PENDING;
        }

        return subscription.getBillingStatus();
    }

    public boolean isTrialActive(StoreSubscription subscription) {
        return subscription.getStatus() == SubscriptionStatus.TRIAL
                && (subscription.getTrialEndsAt() == null || subscription.getTrialEndsAt().isAfter(LocalDateTime.now()));
    }
}