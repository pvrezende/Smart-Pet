package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.StoreSubscriptionResponse;
import com.paulo.smartpet.dto.StoreSubscriptionUpdateRequest;
import com.paulo.smartpet.service.StoreSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-subscriptions")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class StoreSubscriptionController {

    private final StoreSubscriptionService storeSubscriptionService;

    public StoreSubscriptionController(StoreSubscriptionService storeSubscriptionService) {
        this.storeSubscriptionService = storeSubscriptionService;
    }

    @GetMapping
    public List<StoreSubscriptionResponse> list() {
        return storeSubscriptionService.list();
    }

    @GetMapping("/store/{storeId}")
    public StoreSubscriptionResponse getByStoreId(@PathVariable Long storeId) {
        return storeSubscriptionService.getByStoreId(storeId);
    }

    @PutMapping("/store/{storeId}")
    public ApiSuccessResponse<StoreSubscriptionResponse> updateByStoreId(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreSubscriptionUpdateRequest request
    ) {
        return ApiSuccessResponse.of(
                "Assinatura SaaS da loja atualizada com sucesso",
                storeSubscriptionService.updateByStoreId(storeId, request)
        );
    }
}