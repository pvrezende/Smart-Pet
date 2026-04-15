package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CreateSaleRequest;
import com.paulo.smartpet.dto.SaleItemRequest;
import com.paulo.smartpet.entity.*;
import com.paulo.smartpet.repository.CustomerRepository;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.SaleRepository;
import com.paulo.smartpet.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementRepository stockMovementRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository, CustomerRepository customerRepository, StockMovementRepository stockMovementRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Sale> list() {
        return saleRepository.findAll();
    }

    @Transactional
    public Sale create(CreateSaleRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new RuntimeException("Carrinho vazio");
        }

        Sale sale = new Sale();
        if (request.customerId() != null) {
            sale.setCustomer(customerRepository.findById(request.customerId()).orElseThrow(() -> new RuntimeException("Cliente não encontrado")));
        }
        sale.setPaymentMethod(request.paymentMethod());
        sale.setNotes(request.notes());
        sale.setDiscount(request.discount() == null ? BigDecimal.ZERO : request.discount());

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId()).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            if (product.getStock() < itemRequest.quantity()) {
                throw new RuntimeException("Estoque insuficiente para " + product.getName());
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

        sale.setTotalAmount(total);
        sale.setFinalAmount(total.subtract(sale.getDiscount()));
        return saleRepository.save(sale);
    }

    @Transactional
    public Sale cancel(Long id) {
        Sale sale = saleRepository.findById(id).orElseThrow(() -> new RuntimeException("Venda não encontrada"));
        if ("CANCELADA".equals(sale.getStatus())) {
            return sale;
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
        return saleRepository.save(sale);
    }
}
