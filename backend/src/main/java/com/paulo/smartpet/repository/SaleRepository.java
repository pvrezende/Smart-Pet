package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByStoreIdOrderBySaleDateDesc(Long storeId);

    List<Sale> findByStoreIdAndStatusOrderBySaleDateDesc(Long storeId, String status);

    List<Sale> findByStoreIdAndCustomerIdOrderBySaleDateDesc(Long storeId, Long customerId);

    List<Sale> findByStoreIdAndCustomerIdAndStatusOrderBySaleDateDesc(Long storeId, Long customerId, String status);

    List<Sale> findByStoreIdAndSaleDateBetweenOrderBySaleDateDesc(Long storeId, LocalDateTime start, LocalDateTime end);

    List<Sale> findByStoreIdAndSaleDateBetweenAndStatusOrderBySaleDateDesc(Long storeId, LocalDateTime start, LocalDateTime end, String status);

    List<Sale> findByStoreIdAndCustomerIdAndSaleDateBetweenOrderBySaleDateDesc(Long storeId, Long customerId, LocalDateTime start, LocalDateTime end);

    List<Sale> findByStoreIdAndCustomerIdAndSaleDateBetweenAndStatusOrderBySaleDateDesc(
            Long storeId,
            Long customerId,
            LocalDateTime start,
            LocalDateTime end,
            String status
    );

    long countByStoreId(Long storeId);

    long countByStoreIdAndStatus(Long storeId, String status);

    long countByStoreIdAndSaleDateBetweenAndStatus(Long storeId, LocalDateTime start, LocalDateTime end, String status);

    List<Sale> findByStoreIdAndSaleDateBetweenAndStatus(Long storeId, LocalDateTime start, LocalDateTime end, String status);

    @Query("""
            select s
            from Sale s
            where s.store.id = :storeId
              and (:customerId is null or s.customer.id = :customerId)
              and (:status is null or upper(s.status) = :status)
              and (:start is null or s.saleDate >= :start)
              and (:end is null or s.saleDate <= :end)
            """)
    Page<Sale> findPageByFilters(
            Long storeId,
            Long customerId,
            String status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}