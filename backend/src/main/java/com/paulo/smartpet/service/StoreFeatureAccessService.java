package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.SaasFeature;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.FeatureNotAvailableException;
import org.springframework.stereotype.Service;

@Service
public class StoreFeatureAccessService {

    private final AuthenticatedUserService authenticatedUserService;
    private final StoreSubscriptionService storeSubscriptionService;
    private final SaasPlanFeatureService saasPlanFeatureService;

    public StoreFeatureAccessService(
            AuthenticatedUserService authenticatedUserService,
            StoreSubscriptionService storeSubscriptionService,
            SaasPlanFeatureService saasPlanFeatureService
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.storeSubscriptionService = storeSubscriptionService;
        this.saasPlanFeatureService = saasPlanFeatureService;
    }

    public void validateCurrentUserAccess(SaasFeature feature) {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.SUPER_ADMIN || currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        Long storeId = authenticatedUserService.getRequiredStoreId(currentUser);
        StoreSubscription subscription = storeSubscriptionService.getEntityByStoreId(storeId);

        if (!saasPlanFeatureService.hasFeature(subscription.getPlan(), feature)) {
            throw new FeatureNotAvailableException(buildMessage(feature, subscription.getPlan().name()));
        }
    }

    private String buildMessage(SaasFeature feature, String planName) {
        return switch (feature) {
            case ADVANCED_ANALYTICS ->
                    "Recurso indisponível no plano atual (" + planName + "). Faça upgrade para PRO ou ENTERPRISE";
        };
    }
}