package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.SaasPlanResponse;
import com.paulo.smartpet.service.SaasPlanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saas-plans")
public class SaasPlanController {

    private final SaasPlanService saasPlanService;

    public SaasPlanController(SaasPlanService saasPlanService) {
        this.saasPlanService = saasPlanService;
    }

    @GetMapping("/catalog")
    public List<SaasPlanResponse> listActive() {
        return saasPlanService.listActive();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public List<SaasPlanResponse> listAll() {
        return saasPlanService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public SaasPlanResponse getById(@PathVariable Long id) {
        return saasPlanService.getById(id);
    }
}