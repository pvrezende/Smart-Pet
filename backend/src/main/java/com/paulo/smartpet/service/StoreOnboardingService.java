package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CreateUserRequest;
import com.paulo.smartpet.dto.StoreOnboardingRequest;
import com.paulo.smartpet.dto.StoreOnboardingResponse;
import com.paulo.smartpet.dto.StoreRequest;
import com.paulo.smartpet.dto.StoreResponse;
import com.paulo.smartpet.dto.StoreSubscriptionResponse;
import com.paulo.smartpet.dto.UserResponse;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreOnboardingService {

    private final StoreService storeService;
    private final UserService userService;
    private final StoreSubscriptionService storeSubscriptionService;

    public StoreOnboardingService(
            StoreService storeService,
            UserService userService,
            StoreSubscriptionService storeSubscriptionService
    ) {
        this.storeService = storeService;
        this.userService = userService;
        this.storeSubscriptionService = storeSubscriptionService;
    }

    @Transactional
    public StoreOnboardingResponse onboard(StoreOnboardingRequest request) {
        StoreRequest storeRequest = new StoreRequest(
                request.storeName(),
                request.storeCode(),
                request.storeAddress(),
                request.storePhone()
        );

        StoreResponse createdStore = storeService.create(storeRequest);

        CreateUserRequest createUserRequest = new CreateUserRequest(
                request.adminName(),
                request.adminUsername(),
                request.adminPassword(),
                UserRole.ADMIN_STORE,
                createdStore.id()
        );

        UserResponse adminUser = userService.create(createUserRequest);

        Store storeEntity = storeService.getEntityById(createdStore.id());
        StoreSubscriptionResponse subscription = storeSubscriptionService
                .getByStoreId(storeSubscriptionService.ensureSubscriptionExistsForStore(storeEntity).getStore().getId());

        return new StoreOnboardingResponse(
                createdStore,
                adminUser,
                subscription,
                "Loja, administrador inicial e assinatura SaaS criados com sucesso"
        );
    }
}