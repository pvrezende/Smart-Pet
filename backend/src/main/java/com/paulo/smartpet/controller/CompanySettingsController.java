package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.CompanySettingsRequest;
import com.paulo.smartpet.dto.CompanySettingsResponse;
import com.paulo.smartpet.service.CompanySettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
public class CompanySettingsController {

    private final CompanySettingsService companySettingsService;

    public CompanySettingsController(CompanySettingsService companySettingsService) {
        this.companySettingsService = companySettingsService;
    }

    @GetMapping
    public CompanySettingsResponse getCurrent() {
        return companySettingsService.getCurrent();
    }

    @PutMapping
    public CompanySettingsResponse update(@Valid @RequestBody CompanySettingsRequest request) {
        return companySettingsService.update(request);
    }
}