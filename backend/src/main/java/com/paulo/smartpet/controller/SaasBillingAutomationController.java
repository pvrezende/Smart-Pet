package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.SaasBillingAutomationResponse;
import com.paulo.smartpet.service.SaasBillingAutomationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saas-billing-automation")
public class SaasBillingAutomationController {

    private final SaasBillingAutomationService saasBillingAutomationService;

    public SaasBillingAutomationController(SaasBillingAutomationService saasBillingAutomationService) {
        this.saasBillingAutomationService = saasBillingAutomationService;
    }

    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiSuccessResponse<SaasBillingAutomationResponse> execute() {
        return ApiSuccessResponse.of(
                "Automação de cobrança SaaS executada com sucesso",
                saasBillingAutomationService.executeAutomaticChargeGeneration()
        );
    }
}