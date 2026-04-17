package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiPageResponse;
import com.paulo.smartpet.dto.ApiSuccessResponse;
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

    @GetMapping("/page")
    public ApiPageResponse<StoreResponse> listPaged(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        return storeService.listPaged(active, search, page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public StoreResponse getById(@PathVariable Long id) {
        return storeService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiSuccessResponse<StoreResponse> create(@Valid @RequestBody StoreRequest request) {
        return ApiSuccessResponse.of("Loja criada com sucesso", storeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiSuccessResponse<StoreResponse> update(@PathVariable Long id, @Valid @RequestBody StoreRequest request) {
        return ApiSuccessResponse.of("Loja atualizada com sucesso", storeService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        storeService.deactivate(id);
    }
}