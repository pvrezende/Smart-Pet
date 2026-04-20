package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasAdminDashboardResponse;
import com.paulo.smartpet.dto.SaasPlanSummaryResponse;
import com.paulo.smartpet.dto.SaasStatusSummaryResponse;
import com.paulo.smartpet.dto.SaasStoreSummaryResponse;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.repository.StoreRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class SaasAdminService {

    private final StoreRepository storeRepository;
    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final UserRepository userRepository;

    public SaasAdminService(
            StoreRepository storeRepository,
            StoreSubscriptionRepository storeSubscriptionRepository,
            UserRepository userRepository
    ) {
        this.storeRepository = storeRepository;
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.userRepository = userRepository;
    }

    public SaasAdminDashboardResponse getDashboard() {
        List<Store> stores = storeRepository.findAll();
        List<StoreSubscription> subscriptions = storeSubscriptionRepository.findAllByOrderByCreatedAtDesc();

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
}