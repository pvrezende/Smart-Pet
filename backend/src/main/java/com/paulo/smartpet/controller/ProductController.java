package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiPageResponse;
import com.paulo.smartpet.dto.ProductRequest;
import com.paulo.smartpet.dto.ProductResponse;
import com.paulo.smartpet.dto.StockAdjustmentRequest;
import com.paulo.smartpet.dto.StockMovementResponse;
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
    public List<ProductResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String animalType,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        return productService.list(storeId, animalType, active, search);
    }

    @GetMapping("/page")
    public ApiPageResponse<ProductResponse> listPaged(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String animalType,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        return productService.listPaged(storeId, animalType, active, search, page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/barcode/{barcode}")
    public ProductResponse getByBarcode(
            @PathVariable String barcode,
            @RequestParam(required = false) Long storeId
    ) {
        return productService.getByBarcode(storeId, barcode);
    }

    @GetMapping("/barcode-search")
    public List<ProductResponse> searchByBarcode(
            @RequestParam String barcode,
            @RequestParam(required = false) Long storeId
    ) {
        return productService.searchByBarcode(storeId, barcode);
    }

    @GetMapping("/{id}/movements")
    public List<StockMovementResponse> getMovements(@PathVariable Long id) {
        return productService.getMovementsByProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        productService.deactivate(id);
    }

    @PostMapping("/{id}/stock/in")
    public ProductResponse stockIn(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
        return productService.addStock(id, request.quantity(), request.observation());
    }

    @PostMapping("/{id}/stock/out")
    public ProductResponse stockOut(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
        return productService.removeStock(id, request.quantity(), request.observation());
    }
}