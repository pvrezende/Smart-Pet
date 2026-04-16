package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.DashboardResponse;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.repository.CustomerRepository;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final StoreService storeService;

    public DashboardService(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            SaleRepository saleRepository,
            StoreService storeService
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
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

    private BigDecimal getStockValue(Product product) {
        double salePrice = product.getSalePrice() == null ? 0.0 : product.getSalePrice();
        int stock = product.getStock() == null ? 0 : product.getStock();
        return BigDecimal.valueOf(salePrice).multiply(BigDecimal.valueOf(stock));
    }

    private BigDecimal sumFinalAmount(List<com.paulo.smartpet.entity.Sale> sales) {
        return sales.stream()
                .map(sale -> sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}