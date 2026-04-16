package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStoreIdAndActiveTrueOrderByNameAsc(Long storeId);

    List<Product> findByStoreIdAndActiveTrueAndAnimalTypeOrderByNameAsc(Long storeId, String animalType);

    List<Product> findByStoreIdAndActiveOrderByNameAsc(Long storeId, Boolean active);

    List<Product> findByStoreIdAndActiveAndAnimalTypeOrderByNameAsc(Long storeId, Boolean active, String animalType);

    List<Product> findByStoreIdAndActiveTrueAndNameContainingIgnoreCaseOrStoreIdAndActiveTrueAndBrandContainingIgnoreCaseOrderByNameAsc(
            Long storeId1,
            String name,
            Long storeId2,
            String brand
    );

    List<Product> findByStoreIdAndActiveTrueAndAnimalTypeAndNameContainingIgnoreCaseOrStoreIdAndActiveTrueAndAnimalTypeAndBrandContainingIgnoreCaseOrderByNameAsc(
            Long storeId1,
            String animalType1,
            String name,
            Long storeId2,
            String animalType2,
            String brand
    );

    Optional<Product> findByStoreIdAndBarcode(Long storeId, String barcode);

    boolean existsByStoreIdAndBarcode(Long storeId, String barcode);

    boolean existsByStoreIdAndBarcodeAndIdNot(Long storeId, String barcode, Long id);

    List<Product> findByStoreIdAndBarcodeContainingIgnoreCaseOrderByNameAsc(Long storeId, String barcode);
}