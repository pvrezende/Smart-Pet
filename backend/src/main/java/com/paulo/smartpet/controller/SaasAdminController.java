package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.SaasAdminDashboardResponse;
import com.paulo.smartpet.service.SaasAdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saas-admin")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class SaasAdminController {

    private final SaasAdminService saasAdminService;

    public SaasAdminController(SaasAdminService saasAdminService) {
        this.saasAdminService = saasAdminService;
    }

    @GetMapping("/dashboard")
    public SaasAdminDashboardResponse getDashboard() {
        return saasAdminService.getDashboard();
    }
}