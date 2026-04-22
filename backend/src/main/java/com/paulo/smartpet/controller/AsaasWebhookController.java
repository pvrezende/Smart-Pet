package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.asaas.AsaasWebhookEvent;
import com.paulo.smartpet.service.AsaasWebhookService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/asaas")
public class AsaasWebhookController {

    private final AsaasWebhookService asaasWebhookService;

    public AsaasWebhookController(AsaasWebhookService asaasWebhookService) {
        this.asaasWebhookService = asaasWebhookService;
    }

    @PostMapping
    public ApiSuccessResponse<String> receiveWebhook(@RequestBody AsaasWebhookEvent event) {
        return ApiSuccessResponse.of(
                "Webhook ASAAS recebido com sucesso",
                asaasWebhookService.process(event)
        );
    }
}