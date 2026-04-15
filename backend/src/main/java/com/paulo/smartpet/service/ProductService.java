package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.StockMovement;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductService(ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Product> list(String animalType) {
        if (animalType == null || animalType.isBlank()) {
            return productRepository.findByActiveTrueOrderByNameAsc();
        }
        return productRepository.findByActiveTrueAndAnimalTypeOrderByNameAsc(animalType);
    }

    public Product getById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public Product create(Product product) {
        product.setId(null);
        product.setActive(true);
        return productRepository.save(product);
    }

    public Product update(Long id, Product payload) {
        Product product = getById(id);
        product.setName(payload.getName());
        product.setAnimalType(payload.getAnimalType());
        product.setBrand(payload.getBrand());
        product.setWeight(payload.getWeight());
        product.setCostPrice(payload.getCostPrice());
        product.setSalePrice(payload.getSalePrice());
        product.setMinimumStock(payload.getMinimumStock());
        return productRepository.save(product);
    }

    public void deactivate(Long id) {
        Product product = getById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public Product addStock(Long id, Integer quantity, String observation) {
        Product product = getById(id);
        int previous = product.getStock();
        product.setStock(previous + quantity);
        Product saved = productRepository.save(product);
        saveMovement(saved, "ENTRADA", quantity, previous, saved.getStock(), observation);
        return saved;
    }

    @Transactional
    public Product removeStock(Long id, Integer quantity, String observation) {
        Product product = getById(id);
        if (product.getStock() < quantity) {
            throw new RuntimeException("Estoque insuficiente");
        }
        int previous = product.getStock();
        product.setStock(previous - quantity);
        Product saved = productRepository.save(product);
        saveMovement(saved, "SAIDA", quantity, previous, saved.getStock(), observation);
        return saved;
    }

    private void saveMovement(Product product, String type, Integer quantity, Integer previous, Integer current, String observation) {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previous);
        movement.setCurrentStock(current);
        movement.setObservation(observation);
        stockMovementRepository.save(movement);
    }
}
