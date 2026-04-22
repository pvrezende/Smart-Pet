package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.SaasFinancialDashboardResponse;
import com.paulo.smartpet.service.SaasFinancialDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saas-financial")
public class SaasFinancialDashboardController {

    private final SaasFinancialDashboardService saasFinancialDashboardService;

    public SaasFinancialDashboardController(SaasFinancialDashboardService saasFinancialDashboardService) {
        this.saasFinancialDashboardService = saasFinancialDashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiSuccessResponse<SaasFinancialDashboardResponse> getDashboard() {
        return ApiSuccessResponse.of(
                "Dashboard financeiro SaaS carregado com sucesso",
                saasFinancialDashboardService.getDashboard()
        );
    }
}