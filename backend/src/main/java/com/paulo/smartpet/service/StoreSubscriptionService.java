package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.StoreBillingSummaryResponse;
import com.paulo.smartpet.dto.StoreFeatureAvailabilityResponse;
import com.paulo.smartpet.dto.StoreSubscriptionHistoryResponse;
import com.paulo.smartpet.dto.StoreSubscriptionResponse;
import com.paulo.smartpet.dto.StoreSubscriptionUpdateRequest;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.StoreSubscriptionHistory;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.StoreRepository;
import com.paulo.smartpet.repository.StoreSubscriptionHistoryRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreSubscriptionService {

    private static final int DEFAULT_TRIAL_DAYS = 15;
    private static final int DEFAULT_BILLING_DAY = 10;

    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final StoreRepository storeRepository;
    private final SaasPlanFeatureService saasPlanFeatureService;
    private final StoreSubscriptionHistoryRepository storeSubscriptionHistoryRepository;
    private final SaasBillingService saasBillingService;

    public StoreSubscriptionService(
            StoreSubscriptionRepository storeSubscriptionRepository,
            StoreRepository storeRepository,
            SaasPlanFeatureService saasPlanFeatureService,
            StoreSubscriptionHistoryRepository storeSubscriptionHistoryRepository,
            SaasBillingService saasBillingService
    ) {
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.storeRepository = storeRepository;
        this.saasPlanFeatureService = saasPlanFeatureService;
        this.storeSubscriptionHistoryRepository = storeSubscriptionHistoryRepository;
        this.saasBillingService = saasBillingService;
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

    public List<StoreSubscriptionHistoryResponse> getHistoryByStoreId(Long storeId) {
        ensureStoreExists(storeId);

        return storeSubscriptionHistoryRepository.findByStoreIdOrderByChangedAtDesc(storeId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public StoreBillingSummaryResponse getBillingSummaryByStoreId(Long storeId) {
        return saasBillingService.toBillingSummary(getEntityByStoreId(storeId));
    }

    public boolean isBillingBlocked(Long storeId) {
        StoreSubscription subscription = getEntityByStoreId(storeId);
        return subscription.getBillingStatus() == BillingStatus.OVERDUE;
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
                    subscription.setBillingStatus(saasBillingService.resolveInitialBillingStatus(SubscriptionStatus.TRIAL));
                    subscription.setStartsAt(now);
                    subscription.setTrialEndsAt(now.plusDays(DEFAULT_TRIAL_DAYS));
                    subscription.setSubscriptionEndsAt(null);
                    subscription.setBillingDay(DEFAULT_BILLING_DAY);
                    subscription.setNextBillingDate(saasBillingService.calculateNextBillingDate(DEFAULT_BILLING_DAY, LocalDate.now()));
                    subscription.setMonthlyPrice(saasBillingService.getMonthlyPrice(SubscriptionPlan.BASIC));
                    subscription.setNotes("Assinatura inicial criada automaticamente");

                    StoreSubscription saved = storeSubscriptionRepository.save(subscription);

                    saveHistory(
                            store,
                            null,
                            saved.getPlan(),
                            null,
                            saved.getStatus(),
                            "Criação inicial automática da assinatura SaaS"
                    );

                    return saved;
                });
    }

    @Transactional
    public void ensureSubscriptionsForAllStores() {
        storeRepository.findAll().forEach(subscriptionStore -> {
            StoreSubscription subscription = ensureSubscriptionExistsForStore(subscriptionStore);
            boolean changed = false;

            if (subscription.getBillingDay() == null) {
                subscription.setBillingDay(DEFAULT_BILLING_DAY);
                changed = true;
            }

            if (subscription.getNextBillingDate() == null) {
                subscription.setNextBillingDate(
                        saasBillingService.calculateNextBillingDate(subscription.getBillingDay(), LocalDate.now())
                );
                changed = true;
            }

            if (subscription.getMonthlyPrice() == null) {
                subscription.setMonthlyPrice(saasBillingService.getMonthlyPrice(subscription.getPlan()));
                changed = true;
            }

            if (subscription.getBillingStatus() == null) {
                subscription.setBillingStatus(saasBillingService.resolveInitialBillingStatus(subscription.getStatus()));
                changed = true;
            }

            if (changed) {
                storeSubscriptionRepository.save(subscription);
            }
        });
    }

    @Transactional
    public StoreSubscriptionResponse updateByStoreId(Long storeId, StoreSubscriptionUpdateRequest request) {
        StoreSubscription subscription = getEntityByStoreId(storeId);

        SubscriptionPlan previousPlan = subscription.getPlan();
        SubscriptionStatus previousStatus = subscription.getStatus();

        subscription.setPlan(request.plan());
        subscription.setStatus(request.status());
        subscription.setBillingStatus(request.billingStatus());
        subscription.setStartsAt(request.startsAt());
        subscription.setTrialEndsAt(request.trialEndsAt());

        if (request.subscriptionEndsAt() != null) {
            subscription.setSubscriptionEndsAt(request.subscriptionEndsAt());
        } else if (request.status() == SubscriptionStatus.CANCELED) {
            subscription.setSubscriptionEndsAt(LocalDateTime.now());
        } else {
            subscription.setSubscriptionEndsAt(null);
        }

        Integer billingDay = request.billingDay() == null ? DEFAULT_BILLING_DAY : request.billingDay();
        subscription.setBillingDay(billingDay);

        if (request.nextBillingDate() != null) {
            subscription.setNextBillingDate(request.nextBillingDate());
        } else {
            subscription.setNextBillingDate(
                    saasBillingService.calculateNextBillingDate(billingDay, LocalDate.now())
            );
        }

        subscription.setMonthlyPrice(saasBillingService.getMonthlyPrice(request.plan()));
        subscription.setNotes(normalizeBlank(request.notes()));

        StoreSubscription saved = storeSubscriptionRepository.save(subscription);

        if (previousPlan != saved.getPlan() || previousStatus != saved.getStatus() || hasText(request.notes())) {
            saveHistory(
                    saved.getStore(),
                    previousPlan,
                    saved.getPlan(),
                    previousStatus,
                    saved.getStatus(),
                    saved.getNotes()
            );
        }

        return toResponse(saved);
    }

    public StoreSubscription getEntityByStoreId(Long storeId) {
        ensureStoreExists(storeId);

        return storeSubscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura SaaS da loja não encontrada"));
    }

    public StoreSubscriptionResponse toResponsePublic(StoreSubscription subscription) {
        return toResponse(subscription);
    }

    private void ensureStoreExists(Long storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    private void saveHistory(
            Store store,
            SubscriptionPlan previousPlan,
            SubscriptionPlan newPlan,
            SubscriptionStatus previousStatus,
            SubscriptionStatus newStatus,
            String notes
    ) {
        StoreSubscriptionHistory history = new StoreSubscriptionHistory();
        history.setStore(store);
        history.setPreviousPlan(previousPlan);
        history.setNewPlan(newPlan);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setNotes(normalizeBlank(notes));
        storeSubscriptionHistoryRepository.save(history);
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
                subscription.getBillingStatus(),
                subscription.getStartsAt(),
                subscription.getTrialEndsAt(),
                subscription.getSubscriptionEndsAt(),
                subscription.getBillingDay(),
                subscription.getNextBillingDate(),
                subscription.getMonthlyPrice(),
                subscription.getNotes(),
                inTrial,
                activeAccess,
                saasPlanFeatureService.getEnabledFeatures(subscription.getPlan()),
                saasPlanFeatureService.getDisabledFeatures(subscription.getPlan()),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    private StoreSubscriptionHistoryResponse toHistoryResponse(StoreSubscriptionHistory history) {
        return new StoreSubscriptionHistoryResponse(
                history.getId(),
                history.getStore() != null ? history.getStore().getId() : null,
                history.getStore() != null ? history.getStore().getName() : null,
                history.getPreviousPlan(),
                history.getNewPlan(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getNotes(),
                history.getChangedAt()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}