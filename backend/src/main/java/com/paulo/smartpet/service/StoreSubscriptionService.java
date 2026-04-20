package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.StoreFeatureAvailabilityResponse;
import com.paulo.smartpet.dto.StoreSubscriptionResponse;
import com.paulo.smartpet.dto.StoreSubscriptionUpdateRequest;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.StoreRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreSubscriptionService {

    private static final int DEFAULT_TRIAL_DAYS = 15;

    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final StoreRepository storeRepository;
    private final SaasPlanFeatureService saasPlanFeatureService;

    public StoreSubscriptionService(
            StoreSubscriptionRepository storeSubscriptionRepository,
            StoreRepository storeRepository,
            SaasPlanFeatureService saasPlanFeatureService
    ) {
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.storeRepository = storeRepository;
        this.saasPlanFeatureService = saasPlanFeatureService;
    }

    public List<StoreSubscriptionResponse> list() {
        return storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StoreSubscriptionResponse getByStoreId(Long storeId) {
        return toResponse(getEntityByStoreId(storeId));
    }

    public StoreFeatureAvailabilityResponse getFeatureAvailabilityByStoreId(Long storeId) {
        StoreSubscription subscription = getEntityByStoreId(storeId);

        return new StoreFeatureAvailabilityResponse(
                subscription.getStore().getId(),
                subscription.getStore().getName(),
                subscription.getPlan(),
                subscription.getStatus(),
                saasPlanFeatureService.getEnabledFeatures(subscription.getPlan()),
                saasPlanFeatureService.getDisabledFeatures(subscription.getPlan())
        );
    }

    @Transactional
    public StoreSubscription ensureSubscriptionExistsForStore(Store store) {
        return storeSubscriptionRepository.findByStoreId(store.getId())
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();

                    StoreSubscription subscription = new StoreSubscription();
                    subscription.setStore(store);
                    subscription.setPlan(SubscriptionPlan.BASIC);
                    subscription.setStatus(SubscriptionStatus.TRIAL);
                    subscription.setStartsAt(now);
                    subscription.setTrialEndsAt(now.plusDays(DEFAULT_TRIAL_DAYS));
                    subscription.setSubscriptionEndsAt(null);
                    subscription.setNotes("Assinatura inicial criada automaticamente");

                    return storeSubscriptionRepository.save(subscription);
                });
    }

    @Transactional
    public void ensureSubscriptionsForAllStores() {
        storeRepository.findAll().forEach(this::ensureSubscriptionExistsForStore);
    }

    @Transactional
    public StoreSubscriptionResponse updateByStoreId(Long storeId, StoreSubscriptionUpdateRequest request) {
        StoreSubscription subscription = getEntityByStoreId(storeId);

        subscription.setPlan(request.plan());
        subscription.setStatus(request.status());
        subscription.setStartsAt(request.startsAt());
        subscription.setTrialEndsAt(request.trialEndsAt());

        if (request.subscriptionEndsAt() != null) {
            subscription.setSubscriptionEndsAt(request.subscriptionEndsAt());
        } else if (request.status() == SubscriptionStatus.CANCELED) {
            subscription.setSubscriptionEndsAt(LocalDateTime.now());
        } else {
            subscription.setSubscriptionEndsAt(null);
        }

        subscription.setNotes(normalizeBlank(request.notes()));

        return toResponse(storeSubscriptionRepository.save(subscription));
    }

    public StoreSubscription getEntityByStoreId(Long storeId) {
        ensureStoreExists(storeId);

        return storeSubscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura SaaS da loja não encontrada"));
    }

    private void ensureStoreExists(Long storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    private StoreSubscriptionResponse toResponse(StoreSubscription subscription) {
        LocalDateTime now = LocalDateTime.now();

        boolean inTrial = subscription.getStatus() == SubscriptionStatus.TRIAL
                && subscription.getTrialEndsAt() != null
                && subscription.getTrialEndsAt().isAfter(now);

        boolean activeAccess = switch (subscription.getStatus()) {
            case ACTIVE -> true;
            case TRIAL -> subscription.getTrialEndsAt() == null || subscription.getTrialEndsAt().isAfter(now);
            case SUSPENDED, CANCELED -> false;
        };

        return new StoreSubscriptionResponse(
                subscription.getId(),
                subscription.getStore() != null ? subscription.getStore().getId() : null,
                subscription.getStore() != null ? subscription.getStore().getName() : null,
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getStartsAt(),
                subscription.getTrialEndsAt(),
                subscription.getSubscriptionEndsAt(),
                subscription.getNotes(),
                inTrial,
                activeAccess,
                saasPlanFeatureService.getEnabledFeatures(subscription.getPlan()),
                saasPlanFeatureService.getDisabledFeatures(subscription.getPlan()),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}