package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.SaasFeature;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.FeatureNotAvailableException;
import org.springframework.stereotype.Service;

@Service
public class StoreFeatureAccessService {

    private final AuthenticatedUserService authenticatedUserService;
    private final StoreSubscriptionService storeSubscriptionService;

    public StoreFeatureAccessService(
            AuthenticatedUserService authenticatedUserService,
            StoreSubscriptionService storeSubscriptionService
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.storeSubscriptionService = storeSubscriptionService;
    }

    public void validateCurrentUserAccess(SaasFeature feature) {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.SUPER_ADMIN || currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        Long storeId = authenticatedUserService.getRequiredStoreId(currentUser);
        StoreSubscription subscription = storeSubscriptionService.getEntityByStoreId(storeId);

        if (!hasFeature(subscription.getPlan(), feature)) {
            throw new FeatureNotAvailableException(buildMessage(feature, subscription.getPlan()));
        }
    }

    public boolean hasFeature(SubscriptionPlan plan, SaasFeature feature) {
        if (plan == null || feature == null) {
            return false;
        }

        return switch (feature) {
            case ADVANCED_ANALYTICS -> plan == SubscriptionPlan.PRO || plan == SubscriptionPlan.ENTERPRISE;
        };
    }

    private String buildMessage(SaasFeature feature, SubscriptionPlan plan) {
        return switch (feature) {
            case ADVANCED_ANALYTICS ->
                    "Recurso indisponível no plano atual (" + plan + "). Faça upgrade para PRO ou ENTERPRISE";
        };
    }
}