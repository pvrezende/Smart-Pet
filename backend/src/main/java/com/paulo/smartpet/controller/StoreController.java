package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.StoreRequest;
import com.paulo.smartpet.dto.StoreResponse;
import com.paulo.smartpet.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
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