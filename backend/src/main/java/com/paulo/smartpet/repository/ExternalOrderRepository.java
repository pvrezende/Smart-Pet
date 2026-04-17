package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.ExternalOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalOrderRepository extends JpaRepository<ExternalOrder, Long> {
    Optional<ExternalOrder> findByStoreIdAndSourceAndExternalId(Long storeId, String source, String externalId);
    boolean existsByStoreIdAndSourceAndExternalId(Long storeId, String source, String externalId);
    List<ExternalOrder> findByStoreIdOrderByCreatedAtDesc(Long storeId);
    List<ExternalOrder> findByStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, String status);
}