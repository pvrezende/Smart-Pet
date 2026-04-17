package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.ApiPageResponse;
import com.paulo.smartpet.dto.CreateSaleRequest;
import com.paulo.smartpet.dto.SaleCustomerResponse;
import com.paulo.smartpet.dto.SaleDetailsResponse;
import com.paulo.smartpet.dto.SaleItemRequest;
import com.paulo.smartpet.dto.SaleItemResponse;
import com.paulo.smartpet.dto.SaleResponse;
import com.paulo.smartpet.dto.SalesAnalyticsResponse;
import com.paulo.smartpet.dto.SalesHistorySummaryResponse;
import com.paulo.smartpet.dto.SalesSeriesItemResponse;
import com.paulo.smartpet.dto.TopProductResponse;
import com.paulo.smartpet.entity.Customer;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.Sale;
import com.paulo.smartpet.entity.SaleItem;
import com.paulo.smartpet.entity.StockMovement;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.CustomerRepository;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.SaleRepository;
import com.paulo.smartpet.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SaleService {
    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StoreService storeService;

    public SaleService(
            SaleRepository saleRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            StockMovementRepository stockMovementRepository,
            StoreService storeService
    ) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.storeService = storeService;
    }

    public List<SaleResponse> list(Long storeId, Long customerId, String status, LocalDate startDate, LocalDate endDate) {
        List<Sale> sales = findSales(storeId, customerId, status, startDate, endDate);
        return sales.stream().map(this::toResponse).toList();
    }

    public ApiPageResponse<SaleResponse> listPaged(
            Long storeId,
            Long customerId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        Store store = storeService.resolveStore(storeId);
        String normalizedStatus = normalizeStatus(status);

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("Data final não pode ser menor que a data inicial");
        }

        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? 10 : size;

        if (safePage < 0) {
            throw new BusinessException("Página não pode ser negativa");
        }

        if (safeSize < 1 || safeSize > 100) {
            throw new BusinessException("Tamanho da página deve estar entre 1 e 100");
        }

        String safeSortBy = resolveSaleSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));

        Page<Sale> result = saleRepository.findPageByFilters(
                store.getId(),
                customerId,
                normalizedStatus,
                start,
                end,
                pageable
        );

        List<SaleResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new ApiPageResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                result.isEmpty()
        );
    }

    public SalesHistorySummaryResponse getHistorySummary(Long storeId, Long customerId, String status, LocalDate startDate, LocalDate endDate) {
        List<Sale> sales = findSales(storeId, customerId, status, startDate, endDate);

        long salesCount = sales.size();
        long itemsCount = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .mapToLong(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();

        BigDecimal grossAmount = sales.stream()
                .filter(sale -> !"CANCELADA".equalsIgnoreCase(sale.getStatus()))
                .map(sale -> sale.getTotalAmount() == null ? BigDecimal.ZERO : sale.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = sales.stream()
                .filter(sale -> !"CANCELADA".equalsIgnoreCase(sale.getStatus()))
                .map(sale -> sale.getDiscount() == null ? BigDecimal.ZERO : sale.getDiscount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount = sales.stream()
                .filter(sale -> !"CANCELADA".equalsIgnoreCase(sale.getStatus()))
                .map(sale -> sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedSales = sales.stream()
                .filter(sale -> "CONCLUIDA".equalsIgnoreCase(sale.getStatus()))
                .count();

        long canceledSales = sales.stream()
                .filter(sale -> "CANCELADA".equalsIgnoreCase(sale.getStatus()))
                .count();

        return new SalesHistorySummaryResponse(
                salesCount,
                itemsCount,
                grossAmount,
                discountAmount,
                netAmount,
                completedSales,
                canceledSales
        );
    }

    public SalesAnalyticsResponse getSalesAnalytics(
            Long storeId,
            LocalDate startDate,
            LocalDate endDate,
            String periodType,
            Integer top
    ) {
        Store store = storeService.resolveStore(storeId);

        LocalDate effectiveStart = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveEnd = endDate != null ? endDate : LocalDate.now();

        if (effectiveEnd.isBefore(effectiveStart)) {
            throw new BusinessException("Data final não pode ser menor que a data inicial");
        }

        String normalizedPeriodType = normalizePeriodType(periodType);
        int topLimit = top == null ? 5 : top;

        if (topLimit < 1 || topLimit > 20) {
            throw new BusinessException("Quantidade de produtos do ranking deve estar entre 1 e 20");
        }

        List<Sale> sales = saleRepository.findByStoreIdAndSaleDateBetweenAndStatusOrderBySaleDateDesc(
                store.getId(),
                effectiveStart.atStartOfDay(),
                effectiveEnd.atTime(LocalTime.MAX),
                "CONCLUIDA"
        );

        BigDecimal totalAmount = sales.stream()
                .map(sale -> sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SalesSeriesItemResponse> series = "month".equals(normalizedPeriodType)
                ? buildMonthlySeries(sales)
                : buildDailySeries(sales);

        List<TopProductResponse> topProducts = buildTopProducts(sales, topLimit);

        return new SalesAnalyticsResponse(
                normalizedPeriodType,
                totalAmount,
                (long) sales.size(),
                series,
                topProducts
        );
    }

    public SaleDetailsResponse getById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));

        return toDetailsResponse(sale);
    }

    @Transactional
    public SaleDetailsResponse create(CreateSaleRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("Carrinho vazio");
        }

        Store store = storeService.resolveStore(request.storeId());
        Sale sale = new Sale();
        sale.setStore(store);

        if (request.customerId() != null) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

            if (customer.getStore() == null || !customer.getStore().getId().equals(store.getId())) {
                throw new BusinessException("Cliente não pertence à loja informada");
            }

            sale.setCustomer(customer);
        }

        sale.setPaymentMethod(request.paymentMethod().trim());
        sale.setNotes(normalizeBlank(request.notes()));
        sale.setDiscount(request.discount() == null ? BigDecimal.ZERO : request.discount());

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

            if (product.getStore() == null || !product.getStore().getId().equals(store.getId())) {
                throw new BusinessException("Produto não pertence à loja informada");
            }

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BusinessException("Produto inativo não pode ser vendido: " + product.getName());
            }

            if (product.getStock() < itemRequest.quantity()) {
                throw new BusinessException("Estoque insuficiente para " + product.getName());
            }

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(BigDecimal.valueOf(product.getSalePrice()));
            item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));

            sale.getItems().add(item);
            total = total.add(item.getSubtotal());

            int previous = product.getStock();
            product.setStock(previous - itemRequest.quantity());
            productRepository.save(product);

            StockMovement movement = new StockMovement();
            movement.setProduct(product);
            movement.setMovementType("VENDA");
            movement.setQuantity(itemRequest.quantity());
            movement.setPreviousStock(previous);
            movement.setCurrentStock(product.getStock());
            movement.setObservation("Venda");
            stockMovementRepository.save(movement);
        }

        if (sale.getDiscount().compareTo(total) > 0) {
            throw new BusinessException("Desconto não pode ser maior que o total da venda");
        }

        sale.setTotalAmount(total);
        sale.setFinalAmount(total.subtract(sale.getDiscount()));

        Sale saved = saleRepository.save(sale);
        return toDetailsResponse(saved);
    }

    @Transactional
    public SaleDetailsResponse cancel(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada"));

        if ("CANCELADA".equalsIgnoreCase(sale.getStatus())) {
            return toDetailsResponse(sale);
        }

        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            int previous = product.getStock();
            product.setStock(previous + item.getQuantity());
            productRepository.save(product);

            StockMovement movement = new StockMovement();
            movement.setProduct(product);
            movement.setMovementType("CANCELAMENTO");
            movement.setQuantity(item.getQuantity());
            movement.setPreviousStock(previous);
            movement.setCurrentStock(product.getStock());
            movement.setObservation("Cancelamento da venda " + sale.getId());
            stockMovementRepository.save(movement);
        }

        sale.setStatus("CANCELADA");
        Sale saved = saleRepository.save(sale);
        return toDetailsResponse(saved);
    }

    private List<Sale> findSales(Long storeId, Long customerId, String status, LocalDate startDate, LocalDate endDate) {
        Store store = storeService.resolveStore(storeId);
        String normalizedStatus = normalizeStatus(status);

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("Data final não pode ser menor que a data inicial");
        }

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        if (customerId != null && start != null && end != null && normalizedStatus != null) {
            return saleRepository.findByStoreIdAndCustomerIdAndSaleDateBetweenAndStatusOrderBySaleDateDesc(store.getId(), customerId, start, end, normalizedStatus);
        } else if (customerId != null && start != null && end != null) {
            return saleRepository.findByStoreIdAndCustomerIdAndSaleDateBetweenOrderBySaleDateDesc(store.getId(), customerId, start, end);
        } else if (start != null && end != null && normalizedStatus != null) {
            return saleRepository.findByStoreIdAndSaleDateBetweenAndStatusOrderBySaleDateDesc(store.getId(), start, end, normalizedStatus);
        } else if (start != null && end != null) {
            return saleRepository.findByStoreIdAndSaleDateBetweenOrderBySaleDateDesc(store.getId(), start, end);
        } else if (customerId != null && normalizedStatus != null) {
            return saleRepository.findByStoreIdAndCustomerIdAndStatusOrderBySaleDateDesc(store.getId(), customerId, normalizedStatus);
        } else if (customerId != null) {
            return saleRepository.findByStoreIdAndCustomerIdOrderBySaleDateDesc(store.getId(), customerId);
        } else if (normalizedStatus != null) {
            return saleRepository.findByStoreIdAndStatusOrderBySaleDateDesc(store.getId(), normalizedStatus);
        } else {
            return saleRepository.findByStoreIdOrderBySaleDateDesc(store.getId());
        }
    }

    private List<SalesSeriesItemResponse> buildDailySeries(List<Sale> sales) {
        Map<LocalDate, List<Sale>> grouped = sales.stream()
                .collect(java.util.stream.Collectors.groupingBy(sale -> sale.getSaleDate().toLocalDate()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new SalesSeriesItemResponse(
                        entry.getKey().format(DAY_LABEL_FORMAT),
                        sumSaleList(entry.getValue()),
                        (long) entry.getValue().size()
                ))
                .toList();
    }

    private List<SalesSeriesItemResponse> buildMonthlySeries(List<Sale> sales) {
        Map<YearMonth, List<Sale>> grouped = sales.stream()
                .collect(java.util.stream.Collectors.groupingBy(sale -> YearMonth.from(sale.getSaleDate())));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new SalesSeriesItemResponse(
                        entry.getKey().format(MONTH_LABEL_FORMAT),
                        sumSaleList(entry.getValue()),
                        (long) entry.getValue().size()
                ))
                .toList();
    }

    private List<TopProductResponse> buildTopProducts(List<Sale> sales, int topLimit) {
        Map<Long, TopProductAccumulator> accumulatorMap = new LinkedHashMap<>();

        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    continue;
                }

                Long productId = item.getProduct().getId();
                TopProductAccumulator acc = accumulatorMap.computeIfAbsent(
                        productId,
                        id -> new TopProductAccumulator(id, item.getProduct().getName())
                );

                acc.quantitySold += item.getQuantity() == null ? 0 : item.getQuantity();
                acc.totalAmount = acc.totalAmount.add(item.getSubtotal() == null ? BigDecimal.ZERO : item.getSubtotal());
            }
        }

        return accumulatorMap.values().stream()
                .sorted(Comparator.comparing(TopProductAccumulator::getQuantitySold).reversed()
                        .thenComparing(TopProductAccumulator::getTotalAmount).reversed())
                .limit(topLimit)
                .map(acc -> new TopProductResponse(
                        acc.productId,
                        acc.productName,
                        acc.quantitySold,
                        acc.totalAmount
                ))
                .toList();
    }

    private BigDecimal sumSaleList(List<Sale> sales) {
        return sales.stream()
                .map(sale -> sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private SaleResponse toResponse(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleDate(),
                toCustomerResponse(sale.getCustomer()),
                sale.getTotalAmount(),
                sale.getDiscount(),
                sale.getFinalAmount(),
                sale.getPaymentMethod(),
                sale.getStatus(),
                sale.getNotes(),
                sale.getItems() == null ? 0 : sale.getItems().size()
        );
    }

    private SaleDetailsResponse toDetailsResponse(Sale sale) {
        List<SaleItemResponse> items = sale.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new SaleDetailsResponse(
                sale.getId(),
                sale.getSaleDate(),
                toCustomerResponse(sale.getCustomer()),
                sale.getTotalAmount(),
                sale.getDiscount(),
                sale.getFinalAmount(),
                sale.getPaymentMethod(),
                sale.getStatus(),
                sale.getNotes(),
                items
        );
    }

    private SaleItemResponse toItemResponse(SaleItem item) {
        return new SaleItemResponse(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProduct() != null ? item.getProduct().getName() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private SaleCustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return new SaleCustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getCpf()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeStatus(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private String normalizePeriodType(String value) {
        String normalized = normalizeBlank(value);
        if (normalized == null) {
            return "day";
        }

        String lower = normalized.toLowerCase();
        if (!Set.of("day", "month").contains(lower)) {
            throw new BusinessException("Tipo de período inválido. Use day ou month");
        }

        return lower;
    }

    private Sort.Direction resolveSortDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String resolveSaleSortBy(String sortBy) {
        String normalized = normalizeBlank(sortBy);
        Set<String> allowed = Set.of("id", "saleDate", "totalAmount", "discount", "finalAmount", "paymentMethod", "status");

        if (normalized == null) {
            return "saleDate";
        }

        if (!allowed.contains(normalized)) {
            throw new BusinessException("Campo de ordenação inválido para vendas");
        }

        return normalized;
    }

    private static class TopProductAccumulator {
        private final Long productId;
        private final String productName;
        private long quantitySold = 0;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        private TopProductAccumulator(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }

        public Long getQuantitySold() {
            return quantitySold;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }
}