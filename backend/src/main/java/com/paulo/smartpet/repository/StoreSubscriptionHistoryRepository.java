package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.StoreSubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreSubscriptionHistoryRepository extends JpaRepository<StoreSubscriptionHistory, Long> {
    List<StoreSubscriptionHistory> findByStoreIdOrderByChangedAtDesc(Long storeId);
}