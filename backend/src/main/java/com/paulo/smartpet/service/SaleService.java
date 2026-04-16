package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CreateSaleRequest;
import com.paulo.smartpet.dto.SaleCustomerResponse;
import com.paulo.smartpet.dto.SaleDetailsResponse;
import com.paulo.smartpet.dto.SaleItemRequest;
import com.paulo.smartpet.dto.SaleItemResponse;
import com.paulo.smartpet.dto.SaleResponse;
import com.paulo.smartpet.entity.Customer;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.Sale;
import com.paulo.smartpet.entity.SaleItem;
import com.paulo.smartpet.entity.StockMovement;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.CustomerRepository;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.SaleRepository;
import com.paulo.smartpet.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementRepository stockMovementRepository;

    public SaleService(
            SaleRepository saleRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            StockMovementRepository stockMovementRepository
    ) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<SaleResponse> list(Long customerId, String status, LocalDate startDate, LocalDate endDate) {
        String normalizedStatus = normalizeStatus(status);

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("Data final não pode ser menor que a data inicial");
        }

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        List<Sale> sales;

        if (customerId != null && start != null && end != null && normalizedStatus != null) {
            sales = saleRepository.findByCustomerIdAndSaleDateBetweenAndStatusOrderBySaleDateDesc(customerId, start, end, normalizedStatus);
        } else if (customerId != null && start != null && end != null) {
            sales = saleRepository.findByCustomerIdAndSaleDateBetweenOrderBySaleDateDesc(customerId, start, end);
        } else if (start != null && end != null && normalizedStatus != null) {
            sales = saleRepository.findBySaleDateBetweenAndStatusOrderBySaleDateDesc(start, end, normalizedStatus);
        } else if (start != null && end != null) {
            sales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
        } else if (customerId != null && normalizedStatus != null) {
            sales = saleRepository.findByCustomerIdAndStatusOrderBySaleDateDesc(customerId, normalizedStatus);
        } else if (customerId != null) {
            sales = saleRepository.findByCustomerIdOrderBySaleDateDesc(customerId);
        } else if (normalizedStatus != null) {
            sales = saleRepository.findByStatusOrderBySaleDateDesc(normalizedStatus);
        } else {
            sales = saleRepository.findAllByOrderBySaleDateDesc();
        }

        return sales.stream().map(this::toResponse).toList();
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

        Sale sale = new Sale();

        if (request.customerId() != null) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
            sale.setCustomer(customer);
        }

        sale.setPaymentMethod(request.paymentMethod().trim());
        sale.setNotes(normalizeBlank(request.notes()));
        sale.setDiscount(request.discount() == null ? BigDecimal.ZERO : request.discount());

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

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
}