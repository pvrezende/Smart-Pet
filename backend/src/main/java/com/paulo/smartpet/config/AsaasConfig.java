package com.paulo.smartpet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsaasConfig {

    @Value("${asaas.base-url}")
    private String baseUrl;

    @Value("${asaas.api-key}")
    private String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}