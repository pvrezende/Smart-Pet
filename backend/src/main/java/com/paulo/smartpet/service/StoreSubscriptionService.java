package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.StoreBillingSummaryResponse;
import com.paulo.smartpet.dto.StoreFeatureAvailabilityResponse;
import com.paulo.smartpet.dto.StoreSubscriptionBillingAutomationResponse;
import com.paulo.smartpet.dto.StoreSubscriptionBillingHistoryResponse;
import com.paulo.smartpet.dto.StoreSubscriptionHistoryResponse;
import com.paulo.smartpet.dto.StoreSubscriptionResponse;
import com.paulo.smartpet.dto.StoreSubscriptionUpdateRequest;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.StoreSubscriptionBillingHistory;
import com.paulo.smartpet.entity.StoreSubscriptionHistory;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.StoreRepository;
import com.paulo.smartpet.repository.StoreSubscriptionBillingHistoryRepository;
import com.paulo.smartpet.repository.StoreSubscriptionHistoryRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class StoreSubscriptionService {

    private static final int DEFAULT_TRIAL_DAYS = 15;
    private static final int DEFAULT_BILLING_DAY = 10;

    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final StoreRepository storeRepository;
    private final SaasPlanFeatureService saasPlanFeatureService;
    private final StoreSubscriptionHistoryRepository storeSubscriptionHistoryRepository;
    private final StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository;
    private final SaasBillingService saasBillingService;
    private final SaasAuditService saasAuditService;
    private final AuthenticatedUserService authenticatedUserService;

    public StoreSubscriptionService(
            StoreSubscriptionRepository storeSubscriptionRepository,
            StoreRepository storeRepository,
            SaasPlanFeatureService saasPlanFeatureService,
            StoreSubscriptionHistoryRepository storeSubscriptionHistoryRepository,
            StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository,
            SaasBillingService saasBillingService,
            SaasAuditService saasAuditService,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.storeRepository = storeRepository;
        this.saasPlanFeatureService = saasPlanFeatureService;
        this.storeSubscriptionHistoryRepository = storeSubscriptionHistoryRepository;
        this.storeSubscriptionBillingHistoryRepository = storeSubscriptionBillingHistoryRepository;
        this.saasBillingService = saasBillingService;
        this.saasAuditService = saasAuditService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<StoreSubscriptionResponse> list() {
        return findAllEntitiesWithAutomaticBillingRefresh()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StoreSubscription> findAllEntitiesWithAutomaticBillingRefresh() {
        return storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::applyAutomaticBillingRulesIfNeeded)
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

    public List<StoreSubscriptionBillingHistoryResponse> getBillingHistoryByStoreId(Long storeId) {
        ensureStoreExists(storeId);

        return storeSubscriptionBillingHistoryRepository.findByStoreIdOrderByChangedAtDesc(storeId)
                .stream()
                .map(this::toBillingHistoryResponse)
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
                    subscription.setNextBillingDate(
                            saasBillingService.calculateNextBillingDate(DEFAULT_BILLING_DAY, LocalDate.now())
                    );
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

                    saveBillingHistory(
                            store,
                            null,
                            saved.getBillingStatus(),
                            null,
                            saved.getMonthlyPrice(),
                            null,
                            saved.getBillingDay(),
                            null,
                            saved.getNextBillingDate(),
                            "Criação inicial automática da cobrança SaaS"
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

            applyAutomaticBillingRulesIfNeeded(subscription);
        });
    }

    @Transactional
    public StoreSubscriptionBillingAutomationResponse refreshAutomaticBillingRules() {
        User currentUser = authenticatedUserService.getCurrentUser();
        List<StoreSubscription> subscriptions = storeSubscriptionRepository.findAllByOrderByCreatedAtDesc();

        long totalProcessed = subscriptions.size();
        long totalUpdated = 0L;

        for (StoreSubscription subscription : subscriptions) {
            BillingStatus beforeStatus = subscription.getBillingStatus();
            LocalDate beforeNextBillingDate = subscription.getNextBillingDate();

            StoreSubscription updated = applyAutomaticBillingRulesIfNeeded(subscription);

            if (beforeStatus != updated.getBillingStatus()) {
                totalUpdated++;

                saasAuditService.log(
                        currentUser,
                        updated.getStore() != null ? updated.getStore().getId() : null,
                        "AUTO_BILLING_STATUS_REFRESH",
                        "Regra automática aplicada. billingStatus: "
                                + beforeStatus + " -> " + updated.getBillingStatus()
                                + ", nextBillingDate=" + beforeNextBillingDate
                );
            }
        }

        List<StoreSubscription> refreshed = storeSubscriptionRepository.findAllByOrderByCreatedAtDesc();

        long totalOverdueStores = refreshed.stream()
                .filter(subscription -> subscription.getBillingStatus() == BillingStatus.OVERDUE)
                .count();

        long totalPendingStores = refreshed.stream()
                .filter(subscription -> subscription.getBillingStatus() == BillingStatus.PENDING)
                .count();

        return new StoreSubscriptionBillingAutomationResponse(
                totalProcessed,
                totalUpdated,
                totalOverdueStores,
                totalPendingStores,
                LocalDateTime.now()
        );
    }

    @Transactional
    public StoreSubscriptionResponse updateByStoreId(Long storeId, StoreSubscriptionUpdateRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();
        StoreSubscription subscription = getEntityByStoreId(storeId);

        SubscriptionPlan previousPlan = subscription.getPlan();
        SubscriptionStatus previousStatus = subscription.getStatus();
        BillingStatus previousBillingStatus = subscription.getBillingStatus();
        BigDecimal previousMonthlyPrice = subscription.getMonthlyPrice();
        Integer previousBillingDay = subscription.getBillingDay();
        LocalDate previousNextBillingDate = subscription.getNextBillingDate();

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

        subscription = applyAutomaticBillingRulesIfNeeded(subscription);

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

        if (hasBillingChange(
                previousBillingStatus,
                previousMonthlyPrice,
                previousBillingDay,
                previousNextBillingDate,
                saved
        ) || hasText(request.notes())) {
            saveBillingHistory(
                    saved.getStore(),
                    previousBillingStatus,
                    saved.getBillingStatus(),
                    previousMonthlyPrice,
                    saved.getMonthlyPrice(),
                    previousBillingDay,
                    saved.getBillingDay(),
                    previousNextBillingDate,
                    saved.getNextBillingDate(),
                    saved.getNotes()
            );
        }

        saasAuditService.log(
                currentUser,
                saved.getStore() != null ? saved.getStore().getId() : null,
                "UPDATE_SUBSCRIPTION",
                buildSubscriptionAuditDetails(
                        previousPlan,
                        saved.getPlan(),
                        previousStatus,
                        saved.getStatus(),
                        previousBillingStatus,
                        saved.getBillingStatus(),
                        previousMonthlyPrice,
                        saved.getMonthlyPrice(),
                        previousBillingDay,
                        saved.getBillingDay(),
                        previousNextBillingDate,
                        saved.getNextBillingDate(),
                        saved.getNotes()
                )
        );

        return toResponse(saved);
    }

    public StoreSubscription getEntityByStoreId(Long storeId) {
        ensureStoreExists(storeId);

        StoreSubscription subscription = storeSubscriptionRepository.findByStoreId(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura SaaS da loja não encontrada"));

        return applyAutomaticBillingRulesIfNeeded(subscription);
    }

    public StoreSubscriptionResponse toResponsePublic(StoreSubscription subscription) {
        return toResponse(subscription);
    }

    private void ensureStoreExists(Long storeId) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    @Transactional
    protected StoreSubscription applyAutomaticBillingRulesIfNeeded(StoreSubscription subscription) {
        if (subscription.getBillingDay() == null) {
            subscription.setBillingDay(DEFAULT_BILLING_DAY);
        }

        if (subscription.getNextBillingDate() == null) {
            subscription.setNextBillingDate(
                    saasBillingService.calculateNextBillingDate(subscription.getBillingDay(), LocalDate.now())
            );
        }

        if (subscription.getMonthlyPrice() == null) {
            subscription.setMonthlyPrice(saasBillingService.getMonthlyPrice(subscription.getPlan()));
        }

        BillingStatus previousBillingStatus = subscription.getBillingStatus();
        BillingStatus automaticBillingStatus = saasBillingService.resolveAutomaticBillingStatus(subscription);

        if (previousBillingStatus != automaticBillingStatus) {
            subscription.setBillingStatus(automaticBillingStatus);
            subscription = storeSubscriptionRepository.save(subscription);

            saveBillingHistory(
                    subscription.getStore(),
                    previousBillingStatus,
                    subscription.getBillingStatus(),
                    subscription.getMonthlyPrice(),
                    subscription.getMonthlyPrice(),
                    subscription.getBillingDay(),
                    subscription.getBillingDay(),
                    subscription.getNextBillingDate(),
                    subscription.getNextBillingDate(),
                    "Regra automática de vencimento aplicada"
            );
        }

        return subscription;
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

    private void saveBillingHistory(
            Store store,
            BillingStatus previousBillingStatus,
            BillingStatus newBillingStatus,
            BigDecimal previousMonthlyPrice,
            BigDecimal newMonthlyPrice,
            Integer previousBillingDay,
            Integer newBillingDay,
            LocalDate previousNextBillingDate,
            LocalDate newNextBillingDate,
            String notes
    ) {
        StoreSubscriptionBillingHistory history = new StoreSubscriptionBillingHistory();
        history.setStore(store);
        history.setPreviousBillingStatus(previousBillingStatus);
        history.setNewBillingStatus(newBillingStatus);
        history.setPreviousMonthlyPrice(previousMonthlyPrice);
        history.setNewMonthlyPrice(newMonthlyPrice);
        history.setPreviousBillingDay(previousBillingDay);
        history.setNewBillingDay(newBillingDay);
        history.setPreviousNextBillingDate(previousNextBillingDate);
        history.setNewNextBillingDate(newNextBillingDate);
        history.setNotes(normalizeBlank(notes));
        storeSubscriptionBillingHistoryRepository.save(history);
    }

    private boolean hasBillingChange(
            BillingStatus previousBillingStatus,
            BigDecimal previousMonthlyPrice,
            Integer previousBillingDay,
            LocalDate previousNextBillingDate,
            StoreSubscription saved
    ) {
        return previousBillingStatus != saved.getBillingStatus()
                || !sameMoney(previousMonthlyPrice, saved.getMonthlyPrice())
                || !Objects.equals(previousBillingDay, saved.getBillingDay())
                || !Objects.equals(previousNextBillingDate, saved.getNextBillingDate());
    }

    private boolean sameMoney(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
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

    private StoreSubscriptionBillingHistoryResponse toBillingHistoryResponse(StoreSubscriptionBillingHistory history) {
        return new StoreSubscriptionBillingHistoryResponse(
                history.getId(),
                history.getStore() != null ? history.getStore().getId() : null,
                history.getStore() != null ? history.getStore().getName() : null,
                history.getPreviousBillingStatus(),
                history.getNewBillingStatus(),
                history.getPreviousMonthlyPrice(),
                history.getNewMonthlyPrice(),
                history.getPreviousBillingDay(),
                history.getNewBillingDay(),
                history.getPreviousNextBillingDate(),
                history.getNewNextBillingDate(),
                history.getNotes(),
                history.getChangedAt()
        );
    }

    private String buildSubscriptionAuditDetails(
            SubscriptionPlan previousPlan,
            SubscriptionPlan newPlan,
            SubscriptionStatus previousStatus,
            SubscriptionStatus newStatus,
            BillingStatus previousBillingStatus,
            BillingStatus newBillingStatus,
            BigDecimal previousMonthlyPrice,
            BigDecimal newMonthlyPrice,
            Integer previousBillingDay,
            Integer newBillingDay,
            LocalDate previousNextBillingDate,
            LocalDate newNextBillingDate,
            String notes
    ) {
        return "plan: " + previousPlan + " -> " + newPlan
                + ", status: " + previousStatus + " -> " + newStatus
                + ", billingStatus: " + previousBillingStatus + " -> " + newBillingStatus
                + ", monthlyPrice: " + previousMonthlyPrice + " -> " + newMonthlyPrice
                + ", billingDay: " + previousBillingDay + " -> " + newBillingDay
                + ", nextBillingDate: " + previousNextBillingDate + " -> " + newNextBillingDate
                + (hasText(notes) ? ", notes: " + notes : "");
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}