package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiPageResponse;
import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.CreateSaleRequest;
import com.paulo.smartpet.dto.IntegrationSaleRequest;
import com.paulo.smartpet.dto.NfeAuthorizeRequest;
import com.paulo.smartpet.dto.NfeIssueRequest;
import com.paulo.smartpet.dto.NfeRejectRequest;
import com.paulo.smartpet.dto.NfeStatusResponse;
import com.paulo.smartpet.dto.NfeUpdateRequest;
import com.paulo.smartpet.dto.SaleDetailsResponse;
import com.paulo.smartpet.dto.SaleResponse;
import com.paulo.smartpet.dto.SalesAnalyticsResponse;
import com.paulo.smartpet.dto.SalesHistorySummaryResponse;
import com.paulo.smartpet.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<SaleResponse> list(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return saleService.list(storeId, customerId, status, startDate, endDate);
    }

    @GetMapping("/page")
    public ApiPageResponse<SaleResponse> listPaged(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        return saleService.listPaged(storeId, customerId, status, startDate, endDate, page, size, sortBy, sortDir);
    }

    @GetMapping("/analytics")
    public SalesAnalyticsResponse analytics(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) Integer top
    ) {
        return saleService.getSalesAnalytics(storeId, startDate, endDate, periodType, top);
    }

    @GetMapping("/history-summary")
    public SalesHistorySummaryResponse historySummary(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return saleService.getHistorySummary(storeId, customerId, status, startDate, endDate);
    }

    @GetMapping("/{id}")
    public SaleDetailsResponse getById(@PathVariable Long id) {
        return saleService.getById(id);
    }

    @GetMapping("/{id}/nfe-status")
    public NfeStatusResponse getNfeStatus(@PathVariable Long id) {
        return saleService.getNfeStatus(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiSuccessResponse<SaleDetailsResponse> create(@Valid @RequestBody CreateSaleRequest request) {
        return ApiSuccessResponse.of("Venda criada com sucesso", saleService.create(request));
    }

    @PostMapping("/integration")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiSuccessResponse<SaleDetailsResponse> createFromIntegration(@Valid @RequestBody IntegrationSaleRequest request) {
        return ApiSuccessResponse.of("Venda de integração processada com sucesso", saleService.createFromIntegration(request));
    }

    @PatchMapping("/{id}/fiscal")
    public ApiSuccessResponse<SaleDetailsResponse> updateFiscalData(@PathVariable Long id, @Valid @RequestBody NfeUpdateRequest request) {
        return ApiSuccessResponse.of("Dados fiscais da venda atualizados com sucesso", saleService.updateFiscalData(id, request));
    }

    @PostMapping("/{id}/nfe/issue")
    public ApiSuccessResponse<SaleDetailsResponse> requestNfeIssue(@PathVariable Long id, @RequestBody(required = false) NfeIssueRequest request) {
        NfeIssueRequest safeRequest = request == null ? new NfeIssueRequest(null) : request;
        return ApiSuccessResponse.of("Solicitação de emissão fiscal registrada com sucesso", saleService.requestNfeIssue(id, safeRequest));
    }

    @PostMapping("/{id}/nfe/authorize")
    public ApiSuccessResponse<SaleDetailsResponse> authorizeNfe(@PathVariable Long id, @Valid @RequestBody NfeAuthorizeRequest request) {
        return ApiSuccessResponse.of("NF-e autorizada com sucesso", saleService.authorizeNfe(id, request));
    }

    @PostMapping("/{id}/nfe/reject")
    public ApiSuccessResponse<SaleDetailsResponse> rejectNfe(@PathVariable Long id, @Valid @RequestBody NfeRejectRequest request) {
        return ApiSuccessResponse.of("NF-e rejeitada com sucesso", saleService.rejectNfe(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ApiSuccessResponse<SaleDetailsResponse> cancel(@PathVariable Long id) {
        return ApiSuccessResponse.of("Venda cancelada com sucesso", saleService.cancel(id));
    }
}