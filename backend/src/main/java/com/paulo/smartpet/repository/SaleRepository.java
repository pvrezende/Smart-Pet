package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findAllByOrderBySaleDateDesc();

    List<Sale> findByStatusOrderBySaleDateDesc(String status);

    List<Sale> findByCustomerIdOrderBySaleDateDesc(Long customerId);

    List<Sale> findByCustomerIdAndStatusOrderBySaleDateDesc(Long customerId, String status);

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime start, LocalDateTime end);

    List<Sale> findBySaleDateBetweenAndStatusOrderBySaleDateDesc(LocalDateTime start, LocalDateTime end, String status);

    List<Sale> findByCustomerIdAndSaleDateBetweenOrderBySaleDateDesc(Long customerId, LocalDateTime start, LocalDateTime end);

    List<Sale> findByCustomerIdAndSaleDateBetweenAndStatusOrderBySaleDateDesc(
            Long customerId,
            LocalDateTime start,
            LocalDateTime end,
            String status
    );
}