package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByNameAsc();
    List<Product> findByActiveTrueAndAnimalTypeOrderByNameAsc(String animalType);
}
