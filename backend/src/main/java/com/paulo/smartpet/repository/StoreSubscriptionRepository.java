package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.StoreSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreSubscriptionRepository extends JpaRepository<StoreSubscription, Long> {
    Optional<StoreSubscription> findByStoreId(Long storeId);
    boolean existsByStoreId(Long storeId);
    List<StoreSubscription> findAllByOrderByCreatedAtDesc();
}