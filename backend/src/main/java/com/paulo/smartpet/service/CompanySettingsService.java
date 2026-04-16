package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CompanySettingsRequest;
import com.paulo.smartpet.dto.CompanySettingsResponse;
import com.paulo.smartpet.entity.CompanySettings;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.CompanySettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class CompanySettingsService {

    private final CompanySettingsRepository companySettingsRepository;

    public CompanySettingsService(CompanySettingsRepository companySettingsRepository) {
        this.companySettingsRepository = companySettingsRepository;
    }

    public CompanySettingsResponse getCurrent() {
        return toResponse(getCurrentEntity());
    }

    public CompanySettingsResponse update(CompanySettingsRequest request) {
        CompanySettings company = getCurrentEntity();

        company.setTradeName(request.tradeName().trim());
        company.setLegalName(normalizeBlank(request.legalName()));
        company.setCnpj(cleanNumber(request.cnpj()));
        company.setPhone(cleanNumber(request.phone()));
        company.setEmail(normalizeBlank(request.email()));
        company.setAddress(normalizeBlank(request.address()));
        company.setReceiptFooterMessage(normalizeBlank(request.receiptFooterMessage()));

        return toResponse(companySettingsRepository.save(company));
    }

    public CompanySettings ensureDefaultExists() {
        if (companySettingsRepository.count() > 0) {
            return companySettingsRepository.findAll().get(0);
        }

        CompanySettings company = new CompanySettings();
        company.setTradeName("Smart Pet");
        company.setLegalName("Smart Pet Comércio de Produtos Pet");
        company.setCnpj("");
        company.setPhone("");
        company.setEmail("");
        company.setAddress("");
        company.setReceiptFooterMessage("Obrigado pela preferência!");
        return companySettingsRepository.save(company);
    }

    public CompanySettings getCurrentEntity() {
        return companySettingsRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Configuração da empresa não encontrada"));
    }

    private CompanySettingsResponse toResponse(CompanySettings company) {
        return new CompanySettingsResponse(
                company.getId(),
                company.getTradeName(),
                company.getLegalName(),
                company.getCnpj(),
                company.getPhone(),
                company.getEmail(),
                company.getAddress(),
                company.getReceiptFooterMessage()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String cleanNumber(String value) {
        return value == null || value.isBlank() ? null : value.replaceAll("\\D", "");
    }
}