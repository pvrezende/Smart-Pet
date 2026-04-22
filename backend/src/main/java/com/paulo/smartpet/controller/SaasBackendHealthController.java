package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.SaasBackendHealthResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/saas-backend")
public class SaasBackendHealthController {

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ApiSuccessResponse<SaasBackendHealthResponse> health() {
        return ApiSuccessResponse.of(
                "Status do backend SaaS carregado com sucesso",
                new SaasBackendHealthResponse(
                        "Smart Pet",
                        "SaaS Backend",
                        "UP",
                        true,
                        true,
                        true,
                        true,
                        LocalDateTime.now()
                )
        );
    }
}