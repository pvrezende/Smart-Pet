package com.paulo.smartpet.repository;

import com.paulo.smartpet.entity.SaasNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaasNotificationRepository extends JpaRepository<SaasNotification, Long> {

    List<SaasNotification> findAllByOrderByCreatedAtDesc();

    List<SaasNotification> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<SaasNotification> findByStoreIdAndReadOrderByCreatedAtDesc(Long storeId, Boolean read);

    List<SaasNotification> findByReadOrderByCreatedAtDesc(Boolean read);

    Optional<SaasNotification> findFirstByStoreIdAndTypeAndReadFalseOrderByCreatedAtDesc(Long storeId, String type);
}