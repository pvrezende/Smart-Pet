package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasAdminDashboardResponse;
import com.paulo.smartpet.dto.SaasAdminFinancialDashboardResponse;
import com.paulo.smartpet.dto.SaasBillingStatusSummaryResponse;
import com.paulo.smartpet.dto.SaasFinancialPlanSummaryResponse;
import com.paulo.smartpet.dto.SaasFinancialStoreSummaryResponse;
import com.paulo.smartpet.dto.SaasPlanSummaryResponse;
import com.paulo.smartpet.dto.SaasStatusSummaryResponse;
import com.paulo.smartpet.dto.SaasStoreSummaryResponse;
import com.paulo.smartpet.dto.SaasUpcomingBillingResponse;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.repository.StoreRepository;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class SaasAdminService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final SaasBillingService saasBillingService;
    private final StoreSubscriptionService storeSubscriptionService;

    public SaasAdminService(
            StoreRepository storeRepository,
            UserRepository userRepository,
            SaasBillingService saasBillingService,
            StoreSubscriptionService storeSubscriptionService
    ) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.saasBillingService = saasBillingService;
        this.storeSubscriptionService = storeSubscriptionService;
    }

    public SaasAdminDashboardResponse getDashboard() {
        List<Store> stores = storeRepository.findAll();
        List<StoreSubscription> subscriptions = storeSubscriptionService.findAllEntitiesWithAutomaticBillingRefresh();

        long totalStores = stores.size();
        long totalActiveStores = stores.stream().filter(store -> Boolean.TRUE.equals(store.getActive())).count();
        long totalInactiveStores = stores.stream().filter(store -> !Boolean.TRUE.equals(store.getActive())).count();
        long totalUsers = userRepository.count();

        long totalTrialStores = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.TRIAL)
                .count();

        long totalSuspendedStores = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.SUSPENDED)
                .count();

        long totalCanceledStores = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.CANCELED)
                .count();

        List<SaasPlanSummaryResponse> storesByPlan = Arrays.stream(SubscriptionPlan.values())
                .map(plan -> new SaasPlanSummaryResponse(
                        plan,
                        subscriptions.stream().filter(subscription -> subscription.getPlan() == plan).count()
                ))
                .toList();

        List<SaasStatusSummaryResponse> storesByStatus = Arrays.stream(SubscriptionStatus.values())
                .map(status -> new SaasStatusSummaryResponse(
                        status,
                        subscriptions.stream().filter(subscription -> subscription.getStatus() == status).count()
                ))
                .toList();

        List<SaasStoreSummaryResponse> storeSummaries = subscriptions.stream()
                .map(this::toStoreSummary)
                .toList();

        return new SaasAdminDashboardResponse(
                totalStores,
                totalActiveStores,
                totalInactiveStores,
                totalUsers,
                totalTrialStores,
                totalSuspendedStores,
                totalCanceledStores,
                storesByPlan,
                storesByStatus,
                storeSummaries
        );
    }

    public SaasAdminFinancialDashboardResponse getFinancialDashboard() {
        List<StoreSubscription> subscriptions = storeSubscriptionService.findAllEntitiesWithAutomaticBillingRefresh();

        long totalStores = subscriptions.size();

        long totalChargeableStores = subscriptions.stream()
                .filter(this::isChargeableStore)
                .count();

        long totalOverdueStores = subscriptions.stream()
                .filter(this::isOverdueStore)
                .count();

        BigDecimal totalEstimatedMonthlyRevenue = subscriptions.stream()
                .filter(this::isChargeableStore)
                .map(this::safeMonthlyPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SaasBillingStatusSummaryResponse> storesByBillingStatus = Arrays.stream(BillingStatus.values())
                .map(billingStatus -> new SaasBillingStatusSummaryResponse(
                        billingStatus,
                        subscriptions.stream()
                                .filter(subscription -> subscription.getBillingStatus() == billingStatus)
                                .count()
                ))
                .toList();

        List<SaasFinancialPlanSummaryResponse> revenueByPlan = Arrays.stream(SubscriptionPlan.values())
                .map(plan -> {
                    List<StoreSubscription> byPlan = subscriptions.stream()
                            .filter(subscription -> subscription.getPlan() == plan)
                            .toList();

                    BigDecimal estimatedRevenue = byPlan.stream()
                            .filter(this::isChargeableStore)
                            .map(this::safeMonthlyPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new SaasFinancialPlanSummaryResponse(
                            plan,
                            (long) byPlan.size(),
                            estimatedRevenue
                    );
                })
                .toList();

        List<SaasUpcomingBillingResponse> upcomingBillings = subscriptions.stream()
                .filter(subscription -> subscription.getNextBillingDate() != null)
                .filter(subscription -> subscription.getStatus() != SubscriptionStatus.CANCELED)
                .sorted(Comparator.comparing(StoreSubscription::getNextBillingDate)
                        .thenComparing(subscription -> subscription.getStore() != null ? subscription.getStore().getName() : ""))
                .limit(10)
                .map(this::toUpcomingBilling)
                .toList();

        List<SaasFinancialStoreSummaryResponse> stores = subscriptions.stream()
                .sorted(Comparator.comparing(
                        (StoreSubscription subscription) -> {
                            if (subscription.getStore() == null || subscription.getStore().getName() == null) {
                                return "";
                            }
                            return subscription.getStore().getName().toLowerCase();
                        }
                ))
                .map(this::toFinancialStoreSummary)
                .toList();

        return new SaasAdminFinancialDashboardResponse(
                totalStores,
                totalChargeableStores,
                totalOverdueStores,
                totalEstimatedMonthlyRevenue,
                storesByBillingStatus,
                revenueByPlan,
                upcomingBillings,
                stores
        );
    }

    private SaasStoreSummaryResponse toStoreSummary(StoreSubscription subscription) {
        Store store = subscription.getStore();
        LocalDateTime now = LocalDateTime.now();

        boolean inTrial = subscription.getStatus() == SubscriptionStatus.TRIAL
                && subscription.getTrialEndsAt() != null
                && subscription.getTrialEndsAt().isAfter(now);

        boolean activeAccess = switch (subscription.getStatus()) {
            case ACTIVE -> true;
            case TRIAL -> subscription.getTrialEndsAt() == null || subscription.getTrialEndsAt().isAfter(now);
            case SUSPENDED, CANCELED -> false;
        };

        long usersCount = store != null && store.getId() != null
                ? userRepository.findByStoreIdOrderByNameAsc(store.getId()).size()
                : 0L;

        return new SaasStoreSummaryResponse(
                store != null ? store.getId() : null,
                store != null ? store.getName() : null,
                store != null ? store.getCode() : null,
                store != null ? store.getActive() : null,
                subscription.getPlan(),
                subscription.getStatus(),
                inTrial,
                activeAccess,
                usersCount
        );
    }

    private SaasUpcomingBillingResponse toUpcomingBilling(StoreSubscription subscription) {
        Store store = subscription.getStore();

        return new SaasUpcomingBillingResponse(
                store != null ? store.getId() : null,
                store != null ? store.getName() : null,
                store != null ? store.getCode() : null,
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getBillingStatus(),
                subscription.getMonthlyPrice(),
                subscription.getBillingDay(),
                subscription.getNextBillingDate(),
                saasBillingService.isOverdue(subscription.getBillingStatus(), subscription.getNextBillingDate())
        );
    }

    private SaasFinancialStoreSummaryResponse toFinancialStoreSummary(StoreSubscription subscription) {
        Store store = subscription.getStore();

        return new SaasFinancialStoreSummaryResponse(
                store != null ? store.getId() : null,
                store != null ? store.getName() : null,
                store != null ? store.getCode() : null,
                store != null ? store.getActive() : null,
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getBillingStatus(),
                subscription.getMonthlyPrice(),
                subscription.getBillingDay(),
                subscription.getNextBillingDate(),
                saasBillingService.isOverdue(subscription.getBillingStatus(), subscription.getNextBillingDate())
        );
    }

    private boolean isChargeableStore(StoreSubscription subscription) {
        return subscription.getStatus() != SubscriptionStatus.CANCELED
                && subscription.getBillingStatus() != BillingStatus.TRIAL
                && subscription.getBillingStatus() != BillingStatus.CANCELED;
    }

    private boolean isOverdueStore(StoreSubscription subscription) {
        return saasBillingService.isOverdue(subscription.getBillingStatus(), subscription.getNextBillingDate());
    }

    private BigDecimal safeMonthlyPrice(StoreSubscription subscription) {
        return subscription.getMonthlyPrice() == null ? BigDecimal.ZERO : subscription.getMonthlyPrice();
    }
}