package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.SaasAccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StoreAccessGuardService {

    private final StoreSubscriptionService storeSubscriptionService;

    public StoreAccessGuardService(StoreSubscriptionService storeSubscriptionService) {
        this.storeSubscriptionService = storeSubscriptionService;
    }

    public void validateOperationalAccess(User user) {
        if (user == null || user.getRole() == null) {
            throw new SaasAccessDeniedException("Usuário inválido para validação SaaS");
        }

        if (user.getRole() == UserRole.SUPER_ADMIN || user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getStore() == null || user.getStore().getId() == null) {
            throw new SaasAccessDeniedException("Usuário da loja sem vínculo válido com a loja");
        }

        StoreSubscription subscription = storeSubscriptionService.getEntityByStoreId(user.getStore().getId());
        LocalDateTime now = LocalDateTime.now();

        if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) {
            throw new SaasAccessDeniedException("Acesso bloqueado: assinatura da loja está suspensa");
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            throw new SaasAccessDeniedException("Acesso bloqueado: assinatura da loja está cancelada");
        }

        if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
            if (subscription.getTrialEndsAt() != null && !subscription.getTrialEndsAt().isAfter(now)) {
                throw new SaasAccessDeniedException("Acesso bloqueado: período de trial da loja expirou");
            }
        }
    }
}