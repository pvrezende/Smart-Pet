package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.BillingAccessDeniedException;
import com.paulo.smartpet.exception.SaasAccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class SaasAccessControlService {

    private final AuthenticatedUserService authenticatedUserService;
    private final StoreSubscriptionService storeSubscriptionService;

    public SaasAccessControlService(
            AuthenticatedUserService authenticatedUserService,
            StoreSubscriptionService storeSubscriptionService
    ) {
        this.authenticatedUserService = authenticatedUserService;
        this.storeSubscriptionService = storeSubscriptionService;
    }

    public void validateCurrentUserOperationalAccess() {
        User currentUser = authenticatedUserService.getCurrentUser();

        if (currentUser.getRole() == UserRole.SUPER_ADMIN || currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        Long storeId = authenticatedUserService.getRequiredStoreId(currentUser);
        StoreSubscription subscription = storeSubscriptionService.getEntityByStoreId(storeId);

        validateSubscriptionStatus(subscription);
        validateBillingStatus(subscription);
    }

    private void validateSubscriptionStatus(StoreSubscription subscription) {
        switch (subscription.getStatus()) {
            case ACTIVE -> {
                return;
            }
            case TRIAL -> {
                if (subscription.getTrialEndsAt() != null && subscription.getTrialEndsAt().isBefore(java.time.LocalDateTime.now())) {
                    throw new SaasAccessDeniedException("Acesso bloqueado: período de trial da loja expirou");
                }
            }
            case SUSPENDED -> throw new SaasAccessDeniedException("Acesso bloqueado: assinatura da loja está suspensa");
            case CANCELED -> throw new SaasAccessDeniedException("Acesso bloqueado: assinatura da loja está cancelada");
        }
    }

    private void validateBillingStatus(StoreSubscription subscription) {
        if (subscription.getBillingStatus() == BillingStatus.OVERDUE) {
            throw new BillingAccessDeniedException("Acesso bloqueado: loja com cobrança em atraso");
        }
    }
}