package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.ChartSliceResponse;
import com.paulo.smartpet.dto.DashboardResponse;
import com.paulo.smartpet.dto.LowStockItemResponse;
import com.paulo.smartpet.dto.MobileDashboardResponse;
import com.paulo.smartpet.dto.ProductResponse;
import com.paulo.smartpet.dto.ReportsAnalyticsResponse;
import com.paulo.smartpet.dto.SaleCustomerResponse;
import com.paulo.smartpet.dto.SaleResponse;
import com.paulo.smartpet.dto.StoreStockSummaryResponse;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.Sale;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.repository.CustomerRepository;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.SaleRepository;
import com.paulo.smartpet.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final StoreRepository storeRepository;
    private final StoreService storeService;

    public DashboardService(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            SaleRepository saleRepository,
            StoreRepository storeRepository,
            StoreService storeService
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.storeRepository = storeRepository;
        this.storeService = storeService;
    }

    public DashboardResponse getDashboard(Long storeId) {
        Store store = storeService.resolveStore(storeId);
        List<Product> products = productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId());

        long lowStockCount = products.stream()
                .filter(p -> p.getStock() != null && p.getMinimumStock() != null && p.getStock() <= p.getMinimumStock())
                .count();

        BigDecimal stockValue = products.stream()
                .map(this::getStockValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();

        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime endToday = today.atTime(LocalTime.MAX);

        LocalDate startOfWeekDate = today.with(DayOfWeek.MONDAY);
        LocalDateTime startWeek = startOfWeekDate.atStartOfDay();
        LocalDateTime endWeek = today.atTime(LocalTime.MAX);

        LocalDate startOfMonthDate = today.withDayOfMonth(1);
        LocalDateTime startMonth = startOfMonthDate.atStartOfDay();
        LocalDateTime endMonth = today.atTime(LocalTime.MAX);

        long totalSales = saleRepository.countByStoreId(store.getId());
        long completedSales = saleRepository.countByStoreIdAndStatus(store.getId(), "CONCLUIDA");
        long canceledSales = saleRepository.countByStoreIdAndStatus(store.getId(), "CANCELADA");

        long salesCountToday = saleRepository.countByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startToday, endToday, "CONCLUIDA");
        long salesCountWeek = saleRepository.countByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startWeek, endWeek, "CONCLUIDA");
        long salesCountMonth = saleRepository.countByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startMonth, endMonth, "CONCLUIDA");

        BigDecimal salesAmountToday = sumFinalAmount(saleRepository.findByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startToday, endToday, "CONCLUIDA"));
        BigDecimal salesAmountWeek = sumFinalAmount(saleRepository.findByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startWeek, endWeek, "CONCLUIDA"));
        BigDecimal salesAmountMonth = sumFinalAmount(saleRepository.findByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startMonth, endMonth, "CONCLUIDA"));

        return new DashboardResponse(
                (long) products.size(),
                (long) customerRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId()).size(),
                totalSales,
                completedSales,
                canceledSales,
                stockValue,
                lowStockCount,
                salesAmountToday,
                salesAmountWeek,
                salesAmountMonth,
                salesCountToday,
                salesCountWeek,
                salesCountMonth
        );
    }

    public MobileDashboardResponse getMobileDashboard(Long storeId) {
        Store store = storeService.resolveStore(storeId);
        List<Product> products = productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId());

        List<ProductResponse> lowStockProducts = products.stream()
                .filter(p -> p.getStock() != null && p.getMinimumStock() != null && p.getStock() <= p.getMinimumStock())
                .sorted(Comparator.comparing(Product::getStock))
                .limit(5)
                .map(this::toProductResponse)
                .toList();

        LocalDate today = LocalDate.now();
        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime endToday = today.atTime(LocalTime.MAX);
        LocalDateTime startMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endMonth = today.atTime(LocalTime.MAX);

        List<SaleResponse> recentSales = saleRepository.findByStoreIdOrderBySaleDateDesc(store.getId())
                .stream()
                .limit(5)
                .map(this::toSaleResponse)
                .toList();

        BigDecimal salesAmountToday = sumFinalAmount(
                saleRepository.findByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startToday, endToday, "CONCLUIDA")
        );

        BigDecimal salesAmountMonth = sumFinalAmount(
                saleRepository.findByStoreIdAndSaleDateBetweenAndStatus(store.getId(), startMonth, endMonth, "CONCLUIDA")
        );

        return new MobileDashboardResponse(
                store.getId(),
                store.getName(),
                (long) products.size(),
                (long) customerRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId()).size(),
                saleRepository.countByStoreId(store.getId()),
                (long) lowStockProducts.size(),
                salesAmountToday,
                salesAmountMonth,
                lowStockProducts,
                recentSales
        );
    }

    public ReportsAnalyticsResponse getReportsAnalytics(Long storeId) {
        Store baseStore = storeId != null ? storeService.resolveStore(storeId) : null;
        List<Store> stores = baseStore != null
                ? List.of(baseStore)
                : storeRepository.findByActiveTrueOrderByNameAsc();

        List<ChartSliceResponse> salesByPaymentMethod = buildSalesByPaymentMethod(stores);
        List<ChartSliceResponse> salesByStatus = buildSalesByStatus(stores);
        List<LowStockItemResponse> lowStockProducts = buildLowStockProducts(stores);
        List<StoreStockSummaryResponse> storeStockSummary = buildStoreStockSummary(stores);

        return new ReportsAnalyticsResponse(
                salesByPaymentMethod,
                salesByStatus,
                lowStockProducts,
                storeStockSummary
        );
    }

    private List<ChartSliceResponse> buildSalesByPaymentMethod(List<Store> stores) {
        Map<String, PaymentAccumulator> map = new LinkedHashMap<>();

        for (Store store : stores) {
            List<Sale> sales = saleRepository.findByStoreIdOrderBySaleDateDesc(store.getId());
            for (Sale sale : sales) {
                String key = sale.getPaymentMethod() == null || sale.getPaymentMethod().isBlank()
                        ? "NÃO INFORMADO"
                        : sale.getPaymentMethod().trim().toUpperCase();

                PaymentAccumulator acc = map.computeIfAbsent(key, k -> new PaymentAccumulator());
                acc.count++;
                acc.amount = acc.amount.add(sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount());
            }
        }

        return map.entrySet().stream()
                .map(entry -> new ChartSliceResponse(entry.getKey(), entry.getValue().count, entry.getValue().amount))
                .toList();
    }

    private List<ChartSliceResponse> buildSalesByStatus(List<Store> stores) {
        Map<String, PaymentAccumulator> map = new LinkedHashMap<>();

        for (Store store : stores) {
            List<Sale> sales = saleRepository.findByStoreIdOrderBySaleDateDesc(store.getId());
            for (Sale sale : sales) {
                String key = sale.getStatus() == null || sale.getStatus().isBlank()
                        ? "NÃO INFORMADO"
                        : sale.getStatus().trim().toUpperCase();

                PaymentAccumulator acc = map.computeIfAbsent(key, k -> new PaymentAccumulator());
                acc.count++;
                acc.amount = acc.amount.add(sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount());
            }
        }

        return map.entrySet().stream()
                .map(entry -> new ChartSliceResponse(entry.getKey(), entry.getValue().count, entry.getValue().amount))
                .toList();
    }

    private List<LowStockItemResponse> buildLowStockProducts(List<Store> stores) {
        return stores.stream()
                .flatMap(store -> productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId()).stream())
                .filter(p -> p.getStock() != null && p.getMinimumStock() != null && p.getStock() <= p.getMinimumStock())
                .sorted(Comparator
                        .comparing((Product p) -> (p.getMinimumStock() - p.getStock()))
                        .reversed()
                        .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .map(p -> new LowStockItemResponse(
                        p.getId(),
                        p.getName(),
                        p.getAnimalType(),
                        p.getBrand(),
                        p.getStock(),
                        p.getMinimumStock(),
                        p.getMinimumStock() - p.getStock(),
                        p.getStore() != null ? p.getStore().getId() : null,
                        p.getStore() != null ? p.getStore().getName() : null
                ))
                .toList();
    }

    private List<StoreStockSummaryResponse> buildStoreStockSummary(List<Store> stores) {
        return stores.stream()
                .map(store -> {
                    List<Product> products = productRepository.findByStoreIdAndActiveOrderByNameAsc(store.getId(), true);
                    long activeProducts = products.size();
                    long lowStockProducts = products.stream()
                            .filter(p -> p.getStock() != null && p.getMinimumStock() != null && p.getStock() <= p.getMinimumStock())
                            .count();

                    BigDecimal estimatedStockValue = products.stream()
                            .map(this::getStockValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new StoreStockSummaryResponse(
                            store.getId(),
                            store.getName(),
                            (long) products.size(),
                            activeProducts,
                            lowStockProducts,
                            estimatedStockValue
                    );
                })
                .toList();
    }

    private BigDecimal getStockValue(Product product) {
        double salePrice = product.getSalePrice() == null ? 0.0 : product.getSalePrice();
        int stock = product.getStock() == null ? 0 : product.getStock();
        return BigDecimal.valueOf(salePrice).multiply(BigDecimal.valueOf(stock));
    }

    private BigDecimal sumFinalAmount(List<Sale> sales) {
        return sales.stream()
                .map(sale -> sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ProductResponse toProductResponse(Product product) {
        Long storeId = product.getStore() != null ? product.getStore().getId() : null;
        String storeName = product.getStore() != null ? product.getStore().getName() : null;

        boolean lowStock = product.getStock() != null
                && product.getMinimumStock() != null
                && product.getStock() <= product.getMinimumStock();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getAnimalType(),
                product.getBrand(),
                product.getWeight(),
                product.getCostPrice(),
                product.getSalePrice(),
                product.getStock(),
                product.getMinimumStock(),
                product.getBarcode(),
                storeId,
                storeName,
                product.getActive(),
                lowStock
        );
    }

    private SaleResponse toSaleResponse(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleDate(),
                sale.getCustomer() == null ? null : new SaleCustomerResponse(
                        sale.getCustomer().getId(),
                        sale.getCustomer().getName(),
                        sale.getCustomer().getCpf()
                ),
                sale.getTotalAmount(),
                sale.getDiscount(),
                sale.getFinalAmount(),
                sale.getPaymentMethod(),
                sale.getStatus(),
                sale.getNotes(),
                sale.getSource(),
                sale.getExternalId(),
                sale.getItems() == null ? 0 : sale.getItems().size()
        );
    }

    private static class PaymentAccumulator {
        private long count = 0;
        private BigDecimal amount = BigDecimal.ZERO;
    }
}