package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("""
            select p
            from Product p
            where p.store.id = :storeId
              and ((:active is null and p.active = true) or (:active is not null and p.active = :active))
              and (:animalType is null or p.animalType = :animalType)
              and (
                    :search is null
                    or lower(p.name) like lower(concat('%', :search, '%'))
                    or lower(p.brand) like lower(concat('%', :search, '%'))
                  )
            """)
    Page<Product> findPageByFilters(
            Long storeId,
            Boolean active,
            String animalType,
            String search,
            Pageable pageable
    );
}