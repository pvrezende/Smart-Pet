package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrueOrderByNameAsc();

    List<Product> findByActiveTrueAndAnimalTypeOrderByNameAsc(String animalType);

    List<Product> findByActiveOrderByNameAsc(Boolean active);

    List<Product> findByActiveAndAnimalTypeOrderByNameAsc(Boolean active, String animalType);

    List<Product> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndBrandContainingIgnoreCaseOrderByNameAsc(
            String name,
            String brand
    );

    List<Product> findByActiveTrueAndAnimalTypeAndNameContainingIgnoreCaseOrActiveTrueAndAnimalTypeAndBrandContainingIgnoreCaseOrderByNameAsc(
            String animalType1,
            String name,
            String animalType2,
            String brand
    );

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    List<Product> findByBarcodeContainingIgnoreCaseOrderByNameAsc(String barcode);
}