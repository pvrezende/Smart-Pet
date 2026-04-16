package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ProductRequest;
import com.paulo.smartpet.dto.StockAdjustmentRequest;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String animalType) {
        return productService.list(animalType);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        productService.deactivate(id);
    }

    @PostMapping("/{id}/stock/in")
    public Product stockIn(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
        return productService.addStock(id, request.quantity(), request.observation());
    }

    @PostMapping("/{id}/stock/out")
    public Product stockOut(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
        return productService.removeStock(id, request.quantity(), request.observation());
    }
}