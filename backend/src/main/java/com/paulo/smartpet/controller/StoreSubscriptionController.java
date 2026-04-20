package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.StoreBillingSummaryResponse;
import com.paulo.smartpet.dto.StoreFeatureAvailabilityResponse;
import com.paulo.smartpet.dto.StorePlanLimitsResponse;
import com.paulo.smartpet.dto.StoreSubscriptionHistoryResponse;
import com.paulo.smartpet.dto.StoreSubscriptionResponse;
import com.paulo.smartpet.dto.StoreSubscriptionUpdateRequest;
import com.paulo.smartpet.service.StorePlanLimitService;
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
    private final StorePlanLimitService storePlanLimitService;

    public StoreSubscriptionController(
            StoreSubscriptionService storeSubscriptionService,
            StorePlanLimitService storePlanLimitService
    ) {
        this.storeSubscriptionService = storeSubscriptionService;
        this.storePlanLimitService = storePlanLimitService;
    }

    @GetMapping
    public List<StoreSubscriptionResponse> list() {
        return storeSubscriptionService.list();
    }

    @GetMapping("/store/{storeId}")
    public StoreSubscriptionResponse getByStoreId(@PathVariable Long storeId) {
        return storeSubscriptionService.getByStoreId(storeId);
    }

    @GetMapping("/store/{storeId}/features")
    public StoreFeatureAvailabilityResponse getFeatureAvailabilityByStoreId(@PathVariable Long storeId) {
        return storeSubscriptionService.getFeatureAvailabilityByStoreId(storeId);
    }

    @GetMapping("/store/{storeId}/history")
    public List<StoreSubscriptionHistoryResponse> getHistoryByStoreId(@PathVariable Long storeId) {
        return storeSubscriptionService.getHistoryByStoreId(storeId);
    }

    @GetMapping("/store/{storeId}/limits")
    public StorePlanLimitsResponse getLimitsByStoreId(@PathVariable Long storeId) {
        return storePlanLimitService.getLimitsByStoreId(storeId);
    }

    @GetMapping("/store/{storeId}/billing")
    public StoreBillingSummaryResponse getBillingSummaryByStoreId(@PathVariable Long storeId) {
        return storeSubscriptionService.getBillingSummaryByStoreId(storeId);
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