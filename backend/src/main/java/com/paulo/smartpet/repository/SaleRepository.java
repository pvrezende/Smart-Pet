package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
