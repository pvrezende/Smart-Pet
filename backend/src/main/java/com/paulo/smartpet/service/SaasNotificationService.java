package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasNotificationResponse;
import com.paulo.smartpet.entity.SaasNotification;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.SaasNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaasNotificationService {

    private final SaasNotificationRepository saasNotificationRepository;

    public SaasNotificationService(SaasNotificationRepository saasNotificationRepository) {
        this.saasNotificationRepository = saasNotificationRepository;
    }

    @Transactional
    public SaasNotification create(Long storeId, String type, String title, String message) {
        if (storeId != null) {
            SaasNotification existing = saasNotificationRepository
                    .findFirstByStoreIdAndTypeAndReadFalseOrderByCreatedAtDesc(storeId, type)
                    .orElse(null);

            if (existing != null) {
                existing.setTitle(title);
                existing.setMessage(message);
                return saasNotificationRepository.save(existing);
            }
        }

        SaasNotification notification = new SaasNotification();
        notification.setStoreId(storeId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);

        return saasNotificationRepository.save(notification);
    }

    public List<SaasNotificationResponse> listAll() {
        return saasNotificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SaasNotificationResponse> listByStoreId(Long storeId) {
        return saasNotificationRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SaasNotificationResponse> listUnreadByStoreId(Long storeId) {
        return saasNotificationRepository.findByStoreIdAndReadOrderByCreatedAtDesc(storeId, false)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SaasNotificationResponse> listUnreadGlobal() {
        return saasNotificationRepository.findByReadOrderByCreatedAtDesc(false)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SaasNotificationResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Transactional
    public SaasNotificationResponse markAsRead(Long id) {
        SaasNotification notification = getEntityById(id);
        notification.setRead(true);
        return toResponse(saasNotificationRepository.save(notification));
    }

    @Transactional
    public long markAllAsReadByStoreId(Long storeId) {
        List<SaasNotification> notifications = saasNotificationRepository.findByStoreIdAndReadOrderByCreatedAtDesc(storeId, false);

        notifications.forEach(notification -> notification.setRead(true));
        saasNotificationRepository.saveAll(notifications);

        return notifications.size();
    }

    @Transactional
    public long markAllAsReadGlobal() {
        List<SaasNotification> notifications = saasNotificationRepository.findByReadOrderByCreatedAtDesc(false);

        notifications.forEach(notification -> notification.setRead(true));
        saasNotificationRepository.saveAll(notifications);

        return notifications.size();
    }

    public SaasNotification getEntityById(Long id) {
        return saasNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação SaaS não encontrada"));
    }

    private SaasNotificationResponse toResponse(SaasNotification notification) {
        return new SaasNotificationResponse(
                notification.getId(),
                notification.getStoreId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt()
        );
    }
}