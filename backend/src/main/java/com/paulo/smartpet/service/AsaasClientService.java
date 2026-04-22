package com.paulo.smartpet.service;

import com.paulo.smartpet.config.AsaasConfig;
import com.paulo.smartpet.dto.asaas.AsaasCreateCustomerRequest;
import com.paulo.smartpet.dto.asaas.AsaasCreatePaymentRequest;
import com.paulo.smartpet.dto.asaas.AsaasCustomerResponse;
import com.paulo.smartpet.dto.asaas.AsaasPaymentResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AsaasClientService {

    private final RestTemplate restTemplate;
    private final AsaasConfig asaasConfig;

    public AsaasClientService(RestTemplate restTemplate, AsaasConfig asaasConfig) {
        this.restTemplate = restTemplate;
        this.asaasConfig = asaasConfig;
    }

    public AsaasCustomerResponse createCustomer(AsaasCreateCustomerRequest request) {
        String url = asaasConfig.getBaseUrl() + "/customers";

        HttpEntity<AsaasCreateCustomerRequest> entity = new HttpEntity<>(request, buildHeaders());

        ResponseEntity<AsaasCustomerResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AsaasCustomerResponse.class
        );

        return response.getBody();
    }

    public AsaasPaymentResponse createPayment(AsaasCreatePaymentRequest request) {
        String url = asaasConfig.getBaseUrl() + "/payments";

        HttpEntity<AsaasCreatePaymentRequest> entity = new HttpEntity<>(request, buildHeaders());

        ResponseEntity<AsaasPaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AsaasPaymentResponse.class
        );

        return response.getBody();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access_token", asaasConfig.getApiKey());
        return headers;
    }
}