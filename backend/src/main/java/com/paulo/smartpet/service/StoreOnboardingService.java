package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CreateUserRequest;
import com.paulo.smartpet.dto.StoreOnboardingRequest;
import com.paulo.smartpet.dto.StoreOnboardingResponse;
import com.paulo.smartpet.dto.StoreRequest;
import com.paulo.smartpet.dto.StoreResponse;
import com.paulo.smartpet.dto.UserResponse;
import com.paulo.smartpet.entity.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreOnboardingService {

    private final StoreService storeService;
    private final UserService userService;

    public StoreOnboardingService(StoreService storeService, UserService userService) {
        this.storeService = storeService;
        this.userService = userService;
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

        return new StoreOnboardingResponse(
                createdStore,
                adminUser,
                "Loja e administrador inicial criados com sucesso"
        );
    }
}