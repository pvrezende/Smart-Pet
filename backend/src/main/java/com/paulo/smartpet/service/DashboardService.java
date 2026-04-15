package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.DashboardResponse;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.repository.CustomerRepository;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    public DashboardService(ProductRepository productRepository, CustomerRepository customerRepository, SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    public DashboardResponse getDashboard() {
        var products = productRepository.findByActiveTrueOrderByNameAsc();
        long lowStockCount = products.stream().filter(p -> p.getStock() <= p.getMinimumStock()).count();
        BigDecimal stockValue = products.stream()
                .map(this::getStockValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(
                (long) products.size(),
                (long) customerRepository.findByActiveTrueOrderByNameAsc().size(),
                saleRepository.count(),
                stockValue,
                lowStockCount
        );
    }

    private BigDecimal getStockValue(Product product) {
        return BigDecimal.valueOf(product.getSalePrice()).multiply(BigDecimal.valueOf(product.getStock()));
    }
}
