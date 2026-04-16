package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.DashboardResponse;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.Sale;
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

    public DashboardService(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            SaleRepository saleRepository
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    public DashboardResponse getDashboard() {
        List<Product> products = productRepository.findByActiveTrueOrderByNameAsc();

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

        long totalSales = saleRepository.count();
        long completedSales = saleRepository.countByStatus("CONCLUIDA");
        long canceledSales = saleRepository.countByStatus("CANCELADA");

        long salesCountToday = saleRepository.countBySaleDateBetweenAndStatus(startToday, endToday, "CONCLUIDA");
        long salesCountWeek = saleRepository.countBySaleDateBetweenAndStatus(startWeek, endWeek, "CONCLUIDA");
        long salesCountMonth = saleRepository.countBySaleDateBetweenAndStatus(startMonth, endMonth, "CONCLUIDA");

        BigDecimal salesAmountToday = sumFinalAmount(saleRepository.findBySaleDateBetweenAndStatus(startToday, endToday, "CONCLUIDA"));
        BigDecimal salesAmountWeek = sumFinalAmount(saleRepository.findBySaleDateBetweenAndStatus(startWeek, endWeek, "CONCLUIDA"));
        BigDecimal salesAmountMonth = sumFinalAmount(saleRepository.findBySaleDateBetweenAndStatus(startMonth, endMonth, "CONCLUIDA"));

        return new DashboardResponse(
                (long) products.size(),
                (long) customerRepository.findByActiveTrueOrderByNameAsc().size(),
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

    private BigDecimal sumFinalAmount(List<Sale> sales) {
        return sales.stream()
                .map(sale -> sale.getFinalAmount() == null ? BigDecimal.ZERO : sale.getFinalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}