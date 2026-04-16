package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.ProductRequest;
import com.paulo.smartpet.dto.StockMovementResponse;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.StockMovement;
import com.paulo.smartpet.entity.Store;
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
    private final StoreService storeService;

    public ProductService(
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository,
            StoreService storeService
    ) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.storeService = storeService;
    }

    public List<Product> list(Long storeId, String animalType, Boolean active, String search) {
        Store store = storeService.resolveStore(storeId);
        String normalizedAnimalType = normalizeBlank(animalType);
        String normalizedSearch = normalizeBlank(search);

        if (active == null) {
            if (normalizedSearch != null) {
                if (normalizedAnimalType != null) {
                    return productRepository
                            .findByStoreIdAndActiveTrueAndAnimalTypeAndNameContainingIgnoreCaseOrStoreIdAndActiveTrueAndAnimalTypeAndBrandContainingIgnoreCaseOrderByNameAsc(
                                    store.getId(),
                                    normalizedAnimalType.toLowerCase(),
                                    normalizedSearch,
                                    store.getId(),
                                    normalizedAnimalType.toLowerCase(),
                                    normalizedSearch
                            );
                }

                return productRepository
                        .findByStoreIdAndActiveTrueAndNameContainingIgnoreCaseOrStoreIdAndActiveTrueAndBrandContainingIgnoreCaseOrderByNameAsc(
                                store.getId(),
                                normalizedSearch,
                                store.getId(),
                                normalizedSearch
                        );
            }

            if (normalizedAnimalType != null) {
                return productRepository.findByStoreIdAndActiveTrueAndAnimalTypeOrderByNameAsc(store.getId(), normalizedAnimalType.toLowerCase());
            }

            return productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId());
        }

        if (normalizedAnimalType != null) {
            return productRepository.findByStoreIdAndActiveAndAnimalTypeOrderByNameAsc(store.getId(), active, normalizedAnimalType.toLowerCase());
        }

        return productRepository.findByStoreIdAndActiveOrderByNameAsc(store.getId(), active);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    public Product getByBarcode(Long storeId, String barcode) {
        Store store = storeService.resolveStore(storeId);
        String normalizedBarcode = normalizeBarcode(barcode);

        if (normalizedBarcode == null) {
            throw new BusinessException("Código de barras é obrigatório");
        }

        return productRepository.findByStoreIdAndBarcode(store.getId(), normalizedBarcode)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado para o código de barras informado"));
    }

    public List<Product> searchByBarcode(Long storeId, String barcode) {
        Store store = storeService.resolveStore(storeId);
        String normalizedBarcode = normalizeBarcode(barcode);

        if (normalizedBarcode == null) {
            throw new BusinessException("Código de barras é obrigatório");
        }

        return productRepository.findByStoreIdAndBarcodeContainingIgnoreCaseOrderByNameAsc(store.getId(), normalizedBarcode);
    }

    public List<StockMovementResponse> getMovementsByProduct(Long productId) {
        getById(productId);

        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Product create(ProductRequest request) {
        Store store = storeService.resolveStore(request.storeId());
        validateBusinessRules(request, null, store.getId());

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
        product.setBarcode(normalizeBarcode(request.barcode()));
        product.setStore(store);
        product.setActive(true);

        Product saved = productRepository.save(product);

        if (saved.getStock() > 0) {
            saveMovement(saved, "ENTRADA", saved.getStock(), 0, saved.getStock(), "Estoque inicial");
        }

        return saved;
    }

    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);
        Long effectiveStoreId = request.storeId() != null ? request.storeId() : (product.getStore() != null ? product.getStore().getId() : null);
        Store store = storeService.resolveStore(effectiveStoreId);

        validateBusinessRules(request, id, store.getId());

        product.setName(request.name().trim());
        product.setAnimalType(request.animalType().trim().toLowerCase());
        product.setBrand(request.brand().trim());
        product.setWeight(request.weight());
        product.setCostPrice(request.costPrice());
        product.setSalePrice(request.salePrice());
        product.setStock(request.stock());
        product.setMinimumStock(request.minimumStock());
        product.setBarcode(normalizeBarcode(request.barcode()));
        product.setStore(store);

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

    private void validateBusinessRules(ProductRequest request, Long productId, Long storeId) {
        if (request.salePrice() < request.costPrice()) {
            throw new BusinessException("Preço de venda não pode ser menor que o preço de custo");
        }

        String normalizedBarcode = normalizeBarcode(request.barcode());

        if (normalizedBarcode != null) {
            boolean barcodeExists = productId == null
                    ? productRepository.existsByStoreIdAndBarcode(storeId, normalizedBarcode)
                    : productRepository.existsByStoreIdAndBarcodeAndIdNot(storeId, normalizedBarcode, productId);

            if (barcodeExists) {
                throw new BusinessException("Já existe produto cadastrado com este código de barras nesta loja");
            }
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

    private StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProduct() != null ? movement.getProduct().getId() : null,
                movement.getProduct() != null ? movement.getProduct().getName() : null,
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getPreviousStock(),
                movement.getCurrentStock(),
                movement.getObservation(),
                movement.getMovementDate()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeBarcode(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}