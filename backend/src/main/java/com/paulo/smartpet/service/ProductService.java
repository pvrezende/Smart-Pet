package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.ApiPageResponse;
import com.paulo.smartpet.dto.CatalogProductResponse;
import com.paulo.smartpet.dto.CatalogSyncResponse;
import com.paulo.smartpet.dto.ProductRequest;
import com.paulo.smartpet.dto.ProductResponse;
import com.paulo.smartpet.dto.StockMovementResponse;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.StockMovement;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    public List<ProductResponse> list(Long storeId, String animalType, Boolean active, String search) {
        Store store = storeService.resolveStore(storeId);
        String normalizedAnimalType = normalizeBlank(animalType);
        String normalizedSearch = normalizeBlank(search);

        List<Product> products;

        if (active == null) {
            if (normalizedSearch != null) {
                if (normalizedAnimalType != null) {
                    products = productRepository
                            .findByStoreIdAndActiveTrueAndAnimalTypeAndNameContainingIgnoreCaseOrStoreIdAndActiveTrueAndAnimalTypeAndBrandContainingIgnoreCaseOrderByNameAsc(
                                    store.getId(),
                                    normalizedAnimalType.toLowerCase(),
                                    normalizedSearch,
                                    store.getId(),
                                    normalizedAnimalType.toLowerCase(),
                                    normalizedSearch
                            );
                } else {
                    products = productRepository
                            .findByStoreIdAndActiveTrueAndNameContainingIgnoreCaseOrStoreIdAndActiveTrueAndBrandContainingIgnoreCaseOrderByNameAsc(
                                    store.getId(),
                                    normalizedSearch,
                                    store.getId(),
                                    normalizedSearch
                            );
                }
            } else if (normalizedAnimalType != null) {
                products = productRepository.findByStoreIdAndActiveTrueAndAnimalTypeOrderByNameAsc(
                        store.getId(),
                        normalizedAnimalType.toLowerCase()
                );
            } else {
                products = productRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId());
            }
        } else if (normalizedAnimalType != null) {
            products = productRepository.findByStoreIdAndActiveAndAnimalTypeOrderByNameAsc(
                    store.getId(),
                    active,
                    normalizedAnimalType.toLowerCase()
            );
        } else {
            products = productRepository.findByStoreIdAndActiveOrderByNameAsc(store.getId(), active);
        }

        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    public ApiPageResponse<ProductResponse> listPaged(
            Long storeId,
            String animalType,
            Boolean active,
            String search,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        Store store = storeService.resolveStore(storeId);
        String normalizedAnimalType = normalizeBlank(animalType);
        String normalizedSearch = normalizeBlank(search);

        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? 10 : size;

        if (safePage < 0) {
            throw new BusinessException("Página não pode ser negativa");
        }

        if (safeSize < 1 || safeSize > 100) {
            throw new BusinessException("Tamanho da página deve estar entre 1 e 100");
        }

        String safeSortBy = resolveProductSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);

        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy));

        Page<Product> result = productRepository.findPageByFilters(
                store.getId(),
                active,
                normalizedAnimalType != null ? normalizedAnimalType.toLowerCase() : null,
                normalizedSearch,
                pageable
        );

        List<ProductResponse> content = result.getContent()
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

    public CatalogSyncResponse getCatalog(
            Long storeId,
            String animalType,
            Boolean availableOnly,
            String search,
            LocalDateTime updatedAfter
    ) {
        Store store = storeService.resolveStore(storeId);

        List<CatalogProductResponse> products = productRepository.findCatalogByFilters(
                        store.getId(),
                        normalizeAnimalType(animalType),
                        Boolean.TRUE.equals(availableOnly),
                        normalizeBlank(search),
                        updatedAfter
                ).stream()
                .map(this::toCatalogResponse)
                .toList();

        return new CatalogSyncResponse(
                store.getId(),
                store.getCode(),
                store.getName(),
                LocalDateTime.now(),
                products.size(),
                products
        );
    }

    public ProductResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    public ProductResponse getByBarcode(Long storeId, String barcode) {
        Store store = storeService.resolveStore(storeId);
        String normalizedBarcode = normalizeBarcode(barcode);

        if (normalizedBarcode == null) {
            throw new BusinessException("Código de barras é obrigatório");
        }

        Product product = productRepository.findByStoreIdAndBarcode(store.getId(), normalizedBarcode)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado para o código de barras informado"));

        return toResponse(product);
    }

    public List<ProductResponse> searchByBarcode(Long storeId, String barcode) {
        Store store = storeService.resolveStore(storeId);
        String normalizedBarcode = normalizeBarcode(barcode);

        if (normalizedBarcode == null) {
            throw new BusinessException("Código de barras é obrigatório");
        }

        return productRepository.findByStoreIdAndBarcodeContainingIgnoreCaseOrderByNameAsc(store.getId(), normalizedBarcode)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StockMovementResponse> getMovementsByProduct(Long productId) {
        getEntityById(productId);

        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId)
                .stream()
                .map(this::toMovementResponse)
                .toList();
    }

    public ProductResponse create(ProductRequest request) {
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

        return toResponse(saved);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntityById(id);
        Long effectiveStoreId = request.storeId() != null
                ? request.storeId()
                : (product.getStore() != null ? product.getStore().getId() : null);

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

        return toResponse(productRepository.save(product));
    }

    public void deactivate(Long id) {
        Product product = getEntityById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse addStock(Long id, Integer quantity, String observation) {
        Product product = getEntityById(id);

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Não é possível movimentar estoque de produto inativo");
        }

        int previous = product.getStock();
        product.setStock(previous + quantity);
        Product saved = productRepository.save(product);

        saveMovement(saved, "ENTRADA", quantity, previous, saved.getStock(), observation);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse removeStock(Long id, Integer quantity, String observation) {
        Product product = getEntityById(id);

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
        return toResponse(saved);
    }

    public Product getEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
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

    private StockMovementResponse toMovementResponse(StockMovement movement) {
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

    private ProductResponse toResponse(Product product) {
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

    private CatalogProductResponse toCatalogResponse(Product product) {
        return new CatalogProductResponse(
                product.getId(),
                product.getName(),
                product.getAnimalType(),
                product.getBrand(),
                product.getWeight(),
                product.getSalePrice(),
                product.getStock(),
                product.getBarcode(),
                product.getStock() != null && product.getStock() > 0,
                product.getActive(),
                product.getStore() != null ? product.getStore().getCode() : null,
                product.getStore() != null ? product.getStore().getName() : null
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeBarcode(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeAnimalType(String value) {
        String normalized = normalizeBlank(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private Sort.Direction resolveSortDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String resolveProductSortBy(String sortBy) {
        String normalized = normalizeBlank(sortBy);
        Set<String> allowed = Set.of("id", "name", "animalType", "brand", "stock", "salePrice", "minimumStock", "active");

        if (normalized == null) {
            return "name";
        }

        if (!allowed.contains(normalized)) {
            throw new BusinessException("Campo de ordenação inválido para produtos");
        }

        return normalized;
    }
}