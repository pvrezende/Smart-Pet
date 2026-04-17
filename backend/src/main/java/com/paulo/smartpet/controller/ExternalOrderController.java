package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.CreateExternalOrderRequest;
import com.paulo.smartpet.dto.ExternalOrderResponse;
import com.paulo.smartpet.service.ExternalOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/external-orders")
public class ExternalOrderController {

    private final ExternalOrderService externalOrderService;

    public ExternalOrderController(ExternalOrderService externalOrderService) {
        this.externalOrderService = externalOrderService;
    }

    @GetMapping
    public List<ExternalOrderResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status
    ) {
        return externalOrderService.list(storeId, status);
    }

    @GetMapping("/{id}")
    public ExternalOrderResponse getById(@PathVariable Long id) {
        return externalOrderService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiSuccessResponse<ExternalOrderResponse> create(@Valid @RequestBody CreateExternalOrderRequest request) {
        return ApiSuccessResponse.of("Pedido externo criado com sucesso", externalOrderService.create(request));
    }

    @PatchMapping("/{id}/confirm")
    public ApiSuccessResponse<ExternalOrderResponse> confirm(@PathVariable Long id) {
        return ApiSuccessResponse.of("Pedido externo confirmado com sucesso", externalOrderService.confirm(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiSuccessResponse<ExternalOrderResponse> cancel(@PathVariable Long id) {
        return ApiSuccessResponse.of("Pedido externo cancelado com sucesso", externalOrderService.cancel(id));
    }
}