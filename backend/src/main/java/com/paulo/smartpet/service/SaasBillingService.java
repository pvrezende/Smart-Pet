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

    private static final int DEFAULT_BILLING_DAY = 10;
    private static final int BILLING_WARNING_DAYS = 3;
    private static final int TRIAL_WARNING_DAYS = 3;

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
        int safeBillingDay = billingDay == null ? DEFAULT_BILLING_DAY : billingDay;
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

    public LocalDate calculateNextCycleBillingDate(Integer billingDay, LocalDate paidDate) {
        int safeBillingDay = billingDay == null || billingDay < 1 || billingDay > 31
                ? DEFAULT_BILLING_DAY
                : billingDay;

        LocalDate referenceDate = paidDate == null ? LocalDate.now() : paidDate;

        LocalDate nextMonth = referenceDate.plusMonths(1);
        int adjustedDay = Math.min(safeBillingDay, nextMonth.lengthOfMonth());

        return nextMonth.withDayOfMonth(adjustedDay);
    }

    public boolean isOverdue(BillingStatus billingStatus, LocalDate nextBillingDate) {
        return billingStatus == BillingStatus.OVERDUE
                || (nextBillingDate != null && nextBillingDate.isBefore(LocalDate.now()));
    }

    public boolean isBillingDueSoon(LocalDate nextBillingDate) {
        if (nextBillingDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        return !nextBillingDate.isBefore(today)
                && !nextBillingDate.isAfter(today.plusDays(BILLING_WARNING_DAYS));
    }

    public boolean isTrialActive(StoreSubscription subscription) {
        return subscription.getStatus() == SubscriptionStatus.TRIAL
                && (subscription.getTrialEndsAt() == null || subscription.getTrialEndsAt().isAfter(LocalDateTime.now()));
    }

    public boolean isTrialExpired(StoreSubscription subscription) {
        return subscription.getStatus() == SubscriptionStatus.TRIAL
                && subscription.getTrialEndsAt() != null
                && !subscription.getTrialEndsAt().isAfter(LocalDateTime.now());
    }

    public boolean isTrialEndingSoon(StoreSubscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.TRIAL || subscription.getTrialEndsAt() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningLimit = now.plusDays(TRIAL_WARNING_DAYS);

        return subscription.getTrialEndsAt().isAfter(now)
                && !subscription.getTrialEndsAt().isAfter(warningLimit);
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
                overdue,
                subscription.getPaymentProvider(),
                subscription.getExternalCustomerId(),
                subscription.getExternalSubscriptionId(),
                subscription.getExternalBillingId(),
                subscription.getExternalBillingStatus()
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
}