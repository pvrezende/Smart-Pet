package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.ProductRequest;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.StockMovement;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
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
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    public Product create(ProductRequest request) {
        validateBusinessRules(request);

        Product product = new Product();
        product.setId(null);
        product.setName(request.name().trim());
        product.setAnimalType(request.animalType().trim().toLowerCase());
        product.setBrand(request.brand().trim());
        product.setWeight(request.weight());
        product.setCostPrice(request.costPrice());
        product.setSalePrice(request.salePrice());
        product.setStock(request.stock());
        product.setMinimumStock(request.minimumStock());
        product.setActive(true);

        Product saved = productRepository.save(product);

        if (saved.getStock() > 0) {
            saveMovement(saved, "ENTRADA", saved.getStock(), 0, saved.getStock(), "Estoque inicial");
        }

        return saved;
    }

    public Product update(Long id, ProductRequest request) {
        validateBusinessRules(request);

        Product product = getById(id);
        product.setName(request.name().trim());
        product.setAnimalType(request.animalType().trim().toLowerCase());
        product.setBrand(request.brand().trim());
        product.setWeight(request.weight());
        product.setCostPrice(request.costPrice());
        product.setSalePrice(request.salePrice());
        product.setStock(request.stock());
        product.setMinimumStock(request.minimumStock());

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

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Não é possível movimentar estoque de produto inativo");
        }

        int previous = product.getStock();
        product.setStock(previous + quantity);
        Product saved = productRepository.save(product);

        saveMovement(saved, "ENTRADA", quantity, previous, saved.getStock(), observation);
        return saved;
    }

    @Transactional
    public Product removeStock(Long id, Integer quantity, String observation) {
        Product product = getById(id);

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Não é possível movimentar estoque de produto inativo");
        }

        if (product.getStock() < quantity) {
            throw new BusinessException("Estoque insuficiente");
        }

        int previous = product.getStock();
        product.setStock(previous - quantity);
        Product saved = productRepository.save(product);

        saveMovement(saved, "SAIDA", quantity, previous, saved.getStock(), observation);
        return saved;
    }

    private void validateBusinessRules(ProductRequest request) {
        if (request.salePrice() < request.costPrice()) {
            throw new BusinessException("Preço de venda não pode ser menor que o preço de custo");
        }
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