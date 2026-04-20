package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.StorePlanLimitsResponse;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class StorePlanLimitService {

    private final StoreSubscriptionService storeSubscriptionService;
    private final SaasPlanLimitService saasPlanLimitService;
    private final UserRepository userRepository;

    public StorePlanLimitService(
            StoreSubscriptionService storeSubscriptionService,
            SaasPlanLimitService saasPlanLimitService,
            UserRepository userRepository
    ) {
        this.storeSubscriptionService = storeSubscriptionService;
        this.saasPlanLimitService = saasPlanLimitService;
        this.userRepository = userRepository;
    }

    public StorePlanLimitsResponse getLimitsByStoreId(Long storeId) {
        StoreSubscription subscription = storeSubscriptionService.getEntityByStoreId(storeId);

        long currentActiveUsers = userRepository.findByStoreIdOrderByNameAsc(storeId)
                .stream()
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .count();

        int usersLimit = saasPlanLimitService.getUsersLimit(subscription.getPlan());
        long availableUserSlots = Math.max(0, usersLimit - currentActiveUsers);
        boolean canCreateUsers = saasPlanLimitService.canCreateUsers(subscription.getPlan(), currentActiveUsers);

        return new StorePlanLimitsResponse(
                subscription.getStore().getId(),
                subscription.getStore().getName(),
                subscription.getPlan(),
                subscription.getStatus(),
                usersLimit,
                currentActiveUsers,
                availableUserSlots,
                canCreateUsers
        );
    }

    public void validateCanCreateStoreUser(Long storeId) {
        StorePlanLimitsResponse limits = getLimitsByStoreId(storeId);

        if (!Boolean.TRUE.equals(limits.canCreateUsers())) {
            throw new BusinessException(
                    "Limite de usuários ativos atingido para o plano "
                            + limits.plan()
                            + ". Limite atual: "
                            + limits.usersLimit()
            );
        }
    }
}