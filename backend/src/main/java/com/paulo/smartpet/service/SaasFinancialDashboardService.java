package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasFinancialDashboardResponse;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class SaasFinancialDashboardService {

    private final StoreSubscriptionRepository storeSubscriptionRepository;

    public SaasFinancialDashboardService(StoreSubscriptionRepository storeSubscriptionRepository) {
        this.storeSubscriptionRepository = storeSubscriptionRepository;
    }

    public SaasFinancialDashboardResponse getDashboard() {
        List<StoreSubscription> subscriptions = storeSubscriptionRepository.findAllByOrderByCreatedAtDesc();

        long totalSubscriptions = subscriptions.size();
        long totalStores = subscriptions.stream()
                .map(subscription -> subscription.getStore() != null ? subscription.getStore().getId() : null)
                .filter(id -> id != null)
                .distinct()
                .count();

        long activeSubscriptions = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE)
                .count();

        long trialSubscriptions = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.TRIAL)
                .count();

        long overdueSubscriptions = subscriptions.stream()
                .filter(subscription -> subscription.getBillingStatus() == BillingStatus.OVERDUE)
                .count();

        long canceledSubscriptions = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.CANCELED)
                .count();

        BigDecimal estimatedMonthlyRevenue = subscriptions.stream()
                .map(subscription -> safe(subscription.getMonthlyPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal activeMonthlyRevenue = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE)
                .map(subscription -> safe(subscription.getMonthlyPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overdueMonthlyRevenue = subscriptions.stream()
                .filter(subscription -> subscription.getBillingStatus() == BillingStatus.OVERDUE)
                .map(subscription -> safe(subscription.getMonthlyPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTicket = totalSubscriptions == 0
                ? BigDecimal.ZERO
                : estimatedMonthlyRevenue.divide(BigDecimal.valueOf(totalSubscriptions), 2, RoundingMode.HALF_UP);

        Map<SubscriptionPlan, Long> subscriptionsByPlan = new EnumMap<>(SubscriptionPlan.class);
        for (SubscriptionPlan plan : SubscriptionPlan.values()) {
            long count = subscriptions.stream()
                    .filter(subscription -> subscription.getPlan() == plan)
                    .count();
            subscriptionsByPlan.put(plan, count);
        }

        Map<SubscriptionStatus, Long> subscriptionsByStatus = new EnumMap<>(SubscriptionStatus.class);
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            long count = subscriptions.stream()
                    .filter(subscription -> subscription.getStatus() == status)
                    .count();
            subscriptionsByStatus.put(status, count);
        }

        Map<BillingStatus, Long> subscriptionsByBillingStatus = new EnumMap<>(BillingStatus.class);
        for (BillingStatus status : BillingStatus.values()) {
            long count = subscriptions.stream()
                    .filter(subscription -> subscription.getBillingStatus() == status)
                    .count();
            subscriptionsByBillingStatus.put(status, count);
        }

        return new SaasFinancialDashboardResponse(
                totalStores,
                totalSubscriptions,
                activeSubscriptions,
                trialSubscriptions,
                overdueSubscriptions,
                canceledSubscriptions,
                estimatedMonthlyRevenue,
                activeMonthlyRevenue,
                overdueMonthlyRevenue,
                averageTicket,
                subscriptionsByPlan,
                subscriptionsByStatus,
                subscriptionsByBillingStatus
        );
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}