package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.StoreBillingSummaryResponse;
import com.paulo.smartpet.dto.StoreFeatureAvailabilityResponse;
import com.paulo.smartpet.dto.StorePlanLimitsResponse;
import com.paulo.smartpet.dto.StoreSubscriptionBillingAutomationResponse;
import com.paulo.smartpet.dto.StoreSubscriptionBillingHistoryResponse;
import com.paulo.smartpet.dto.StoreSubscriptionGeneratePaymentRequest;
import com.paulo.smartpet.dto.StoreSubscriptionHistoryResponse;
import com.paulo.smartpet.dto.StoreSubscriptionPaymentLinkResponse;
import com.paulo.smartpet.dto.StoreSubscriptionResponse;
import com.paulo.smartpet.dto.StoreSubscriptionUpdateRequest;
import com.paulo.smartpet.service.StorePlanLimitService;
import com.paulo.smartpet.service.StoreSubscriptionPaymentService;
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
    private final StoreSubscriptionPaymentService storeSubscriptionPaymentService;

    public StoreSubscriptionController(
            StoreSubscriptionService storeSubscriptionService,
            StorePlanLimitService storePlanLimitService,
            StoreSubscriptionPaymentService storeSubscriptionPaymentService
    ) {
        this.storeSubscriptionService = storeSubscriptionService;
        this.storePlanLimitService = storePlanLimitService;
        this.storeSubscriptionPaymentService = storeSubscriptionPaymentService;
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

    @GetMapping("/store/{storeId}/billing")
    public StoreBillingSummaryResponse getBillingSummaryByStoreId(@PathVariable Long storeId) {
        return storeSubscriptionService.getBillingSummaryByStoreId(storeId);
    }

    @GetMapping("/store/{storeId}/billing-history")
    public List<StoreSubscriptionBillingHistoryResponse> getBillingHistoryByStoreId(@PathVariable Long storeId) {
        return storeSubscriptionService.getBillingHistoryByStoreId(storeId);
    }

    @PostMapping("/billing/refresh-rules")
    public ApiSuccessResponse<StoreSubscriptionBillingAutomationResponse> refreshBillingRules() {
        return ApiSuccessResponse.of(
                "Regras automáticas de vencimento processadas com sucesso",
                storeSubscriptionService.refreshAutomaticBillingRules()
        );
    }

    @GetMapping("/store/{storeId}/limits")
    public StorePlanLimitsResponse getLimitsByStoreId(@PathVariable Long storeId) {
        return storePlanLimitService.getLimitsByStoreId(storeId);
    }

    @PostMapping("/store/{storeId}/generate-payment")
    public ApiSuccessResponse<StoreSubscriptionPaymentLinkResponse> generatePayment(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreSubscriptionGeneratePaymentRequest request
    ) {
        return ApiSuccessResponse.of(
                "Cobrança gerada com sucesso no ASAAS",
                storeSubscriptionPaymentService.generatePaymentLink(storeId, request.billingType())
        );
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