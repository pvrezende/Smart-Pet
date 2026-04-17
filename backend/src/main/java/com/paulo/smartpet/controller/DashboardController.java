package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.DashboardResponse;
import com.paulo.smartpet.dto.MobileDashboardResponse;
import com.paulo.smartpet.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@RequestParam(required = false) Long storeId) {
        return dashboardService.getDashboard(storeId);
    }

    @GetMapping("/mobile")
    public MobileDashboardResponse getMobileDashboard(@RequestParam(required = false) Long storeId) {
        return dashboardService.getMobileDashboard(storeId);
    }
}