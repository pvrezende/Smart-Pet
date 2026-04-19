package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.StoreOnboardingRequest;
import com.paulo.smartpet.dto.StoreOnboardingResponse;
import com.paulo.smartpet.dto.StoreRequest;
import com.paulo.smartpet.dto.StoreResponse;
import com.paulo.smartpet.service.StoreOnboardingService;
import com.paulo.smartpet.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class StoreController {

    private final StoreService storeService;
    private final StoreOnboardingService storeOnboardingService;

    public StoreController(StoreService storeService, StoreOnboardingService storeOnboardingService) {
        this.storeService = storeService;
        this.storeOnboardingService = storeOnboardingService;
    }

    @GetMapping
    public List<StoreResponse> list() {
        return storeService.list();
    }

    @GetMapping("/{id}")
    public StoreResponse getById(@PathVariable Long id) {
        return storeService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponse create(@Valid @RequestBody StoreRequest request) {
        return storeService.create(request);
    }

    @PostMapping("/onboarding")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreOnboardingResponse onboard(@Valid @RequestBody StoreOnboardingRequest request) {
        return storeOnboardingService.onboard(request);
    }

    @PutMapping("/{id}")
    public StoreResponse update(@PathVariable Long id, @Valid @RequestBody StoreRequest request) {
        return storeService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        storeService.deactivate(id);
    }
}