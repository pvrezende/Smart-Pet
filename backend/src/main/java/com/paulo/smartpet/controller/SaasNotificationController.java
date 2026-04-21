package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.SaasNotificationResponse;
import com.paulo.smartpet.service.SaasNotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saas-notifications")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
public class SaasNotificationController {

    private final SaasNotificationService saasNotificationService;

    public SaasNotificationController(SaasNotificationService saasNotificationService) {
        this.saasNotificationService = saasNotificationService;
    }

    @GetMapping
    public List<SaasNotificationResponse> listAll() {
        return saasNotificationService.listAll();
    }

    @GetMapping("/unread")
    public List<SaasNotificationResponse> listUnreadGlobal() {
        return saasNotificationService.listUnreadGlobal();
    }

    @GetMapping("/{id}")
    public SaasNotificationResponse getById(@PathVariable Long id) {
        return saasNotificationService.getById(id);
    }

    @GetMapping("/store/{storeId}")
    public List<SaasNotificationResponse> listByStoreId(@PathVariable Long storeId) {
        return saasNotificationService.listByStoreId(storeId);
    }

    @GetMapping("/store/{storeId}/unread")
    public List<SaasNotificationResponse> listUnreadByStoreId(@PathVariable Long storeId) {
        return saasNotificationService.listUnreadByStoreId(storeId);
    }

    @PatchMapping("/{id}/read")
    public ApiSuccessResponse<SaasNotificationResponse> markAsRead(@PathVariable Long id) {
        return ApiSuccessResponse.of(
                "Notificação marcada como lida com sucesso",
                saasNotificationService.markAsRead(id)
        );
    }

    @PatchMapping("/store/{storeId}/read-all")
    public ApiSuccessResponse<Long> markAllAsReadByStoreId(@PathVariable Long storeId) {
        return ApiSuccessResponse.of(
                "Notificações da loja marcadas como lidas com sucesso",
                saasNotificationService.markAllAsReadByStoreId(storeId)
        );
    }

    @PatchMapping("/read-all")
    public ApiSuccessResponse<Long> markAllAsReadGlobal() {
        return ApiSuccessResponse.of(
                "Notificações globais marcadas como lidas com sucesso",
                saasNotificationService.markAllAsReadGlobal()
        );
    }
}