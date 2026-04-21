package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.StoreSubscriptionBillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreSubscriptionBillingHistoryRepository extends JpaRepository<StoreSubscriptionBillingHistory, Long> {
    List<StoreSubscriptionBillingHistory> findByStoreIdOrderByChangedAtDesc(Long storeId);
}