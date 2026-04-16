package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.CreateSaleRequest;
import com.paulo.smartpet.entity.Sale;
import com.paulo.smartpet.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<Sale> list() {
        return saleService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sale create(@Valid @RequestBody CreateSaleRequest request) {
        return saleService.create(request);
    }

    @PatchMapping("/{id}/cancel")
    public Sale cancel(@PathVariable Long id) {
        return saleService.cancel(id);
    }
}