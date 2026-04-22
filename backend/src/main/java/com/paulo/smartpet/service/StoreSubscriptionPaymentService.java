package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.StoreSubscriptionPaymentLinkResponse;
import com.paulo.smartpet.dto.asaas.AsaasCreateCustomerRequest;
import com.paulo.smartpet.dto.asaas.AsaasCreatePaymentRequest;
import com.paulo.smartpet.dto.asaas.AsaasCustomerResponse;
import com.paulo.smartpet.dto.asaas.AsaasPaymentResponse;
import com.paulo.smartpet.entity.CompanySettings;
import com.paulo.smartpet.entity.PaymentProvider;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.StoreSubscriptionBillingHistory;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.StoreRepository;
import com.paulo.smartpet.repository.StoreSubscriptionBillingHistoryRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class StoreSubscriptionPaymentService {

    private static final DateTimeFormatter ASAAS_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Set<String> ALLOWED_BILLING_TYPES = Set.of("PIX", "BOLETO", "CREDIT_CARD");
    private static final int MAX_HISTORY_NOTES_LENGTH = 255;

    private final StoreRepository storeRepository;
    private final StoreSubscriptionRepository storeSubscriptionRepository;
    private final StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository;
    private final StoreSubscriptionService storeSubscriptionService;
    private final CompanySettingsService companySettingsService;
    private final AsaasClientService asaasClientService;

    public StoreSubscriptionPaymentService(
            StoreRepository storeRepository,
            StoreSubscriptionRepository storeSubscriptionRepository,
            StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository,
            StoreSubscriptionService storeSubscriptionService,
            CompanySettingsService companySettingsService,
            AsaasClientService asaasClientService
    ) {
        this.storeRepository = storeRepository;
        this.storeSubscriptionRepository = storeSubscriptionRepository;
        this.storeSubscriptionBillingHistoryRepository = storeSubscriptionBillingHistoryRepository;
        this.storeSubscriptionService = storeSubscriptionService;
        this.companySettingsService = companySettingsService;
        this.asaasClientService = asaasClientService;
    }

    @Transactional
    public StoreSubscriptionPaymentLinkResponse generatePaymentLink(Long storeId, String billingType) {
        String normalizedBillingType = normalizeBillingType(billingType);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));

        StoreSubscription subscription = storeSubscriptionService.getEntityByStoreId(storeId);
        CompanySettings companySettings = companySettingsService.getCurrentEntity();

        String cnpj = cleanNumber(companySettings.getCnpj());
        if (cnpj == null || cnpj.isBlank()) {
            throw new BusinessException("Informe o CNPJ da empresa em /api/company antes de gerar cobrança no ASAAS");
        }

        if (subscription.getMonthlyPrice() == null) {
            throw new BusinessException("A assinatura da loja está sem monthlyPrice configurado");
        }

        PaymentProvider previousPaymentProvider = subscription.getPaymentProvider();
        String previousExternalCustomerId = subscription.getExternalCustomerId();
        String previousExternalSubscriptionId = subscription.getExternalSubscriptionId();
        String previousExternalBillingId = subscription.getExternalBillingId();
        String previousExternalBillingStatus = subscription.getExternalBillingStatus();

        LocalDate dueDate = subscription.getNextBillingDate();
        if (dueDate == null || !dueDate.isAfter(LocalDate.now().minusDays(1))) {
            Integer billingDay = subscription.getBillingDay();

            if (billingDay == null || billingDay < 1 || billingDay > 31) {
                billingDay = 10;
            }

            LocalDate today = LocalDate.now();
            int safeDay = Math.min(billingDay, today.lengthOfMonth());
            LocalDate candidate = today.withDayOfMonth(safeDay);

            if (!candidate.isAfter(today)) {
                LocalDate nextMonth = today.plusMonths(1);
                safeDay = Math.min(billingDay, nextMonth.lengthOfMonth());
                candidate = nextMonth.withDayOfMonth(safeDay);
            }

            dueDate = candidate;
            subscription.setNextBillingDate(dueDate);
            storeSubscriptionRepository.save(subscription);
        }

        String externalCustomerId = subscription.getExternalCustomerId();

        if (externalCustomerId == null || externalCustomerId.isBlank()) {
            AsaasCreateCustomerRequest customerRequest = new AsaasCreateCustomerRequest(
                    hasText(companySettings.getTradeName()) ? companySettings.getTradeName() : store.getName(),
                    cnpj,
                    normalizeBlank(companySettings.getEmail()),
                    cleanNumber(companySettings.getPhone()),
                    cleanNumber(companySettings.getPhone())
            );

            AsaasCustomerResponse customerResponse = asaasClientService.createCustomer(customerRequest);
            externalCustomerId = customerResponse.id();
            subscription.setExternalCustomerId(externalCustomerId);
            storeSubscriptionRepository.save(subscription);
        }

        AsaasCreatePaymentRequest paymentRequest = new AsaasCreatePaymentRequest(
                externalCustomerId,
                normalizedBillingType,
                subscription.getMonthlyPrice(),
                dueDate.format(ASAAS_DATE_FORMAT),
                "Cobrança da assinatura SaaS da loja " + store.getName(),
                "store-" + store.getId() + "-subscription-" + subscription.getId()
        );

        AsaasPaymentResponse paymentResponse = asaasClientService.createPayment(paymentRequest);

        subscription.setPaymentProvider(PaymentProvider.ASAAS);
        subscription.setExternalBillingId(paymentResponse.id());
        subscription.setExternalBillingStatus(paymentResponse.status());
        storeSubscriptionRepository.save(subscription);

        saveBillingHistory(
                subscription,
                previousPaymentProvider,
                previousExternalCustomerId,
                previousExternalSubscriptionId,
                previousExternalBillingId,
                previousExternalBillingStatus,
                normalizedBillingType,
                paymentResponse.id(),
                paymentResponse.status()
        );

        return new StoreSubscriptionPaymentLinkResponse(
                store.getId(),
                store.getName(),
                PaymentProvider.ASAAS,
                externalCustomerId,
                subscription.getExternalSubscriptionId(),
                paymentResponse.id(),
                paymentResponse.status(),
                paymentResponse.billingType(),
                paymentResponse.value(),
                paymentResponse.dueDate(),
                paymentResponse.invoiceUrl(),
                paymentResponse.bankSlipUrl(),
                paymentResponse.transactionReceiptUrl()
        );
    }

    private void saveBillingHistory(
            StoreSubscription subscription,
            PaymentProvider previousPaymentProvider,
            String previousExternalCustomerId,
            String previousExternalSubscriptionId,
            String previousExternalBillingId,
            String previousExternalBillingStatus,
            String billingType,
            String newExternalBillingId,
            String newExternalBillingStatus
    ) {
        StoreSubscriptionBillingHistory history = new StoreSubscriptionBillingHistory();
        history.setStore(subscription.getStore());
        history.setPreviousBillingStatus(subscription.getBillingStatus());
        history.setNewBillingStatus(subscription.getBillingStatus());
        history.setPreviousMonthlyPrice(subscription.getMonthlyPrice());
        history.setNewMonthlyPrice(subscription.getMonthlyPrice());
        history.setPreviousBillingDay(subscription.getBillingDay());
        history.setNewBillingDay(subscription.getBillingDay());
        history.setPreviousNextBillingDate(subscription.getNextBillingDate());
        history.setNewNextBillingDate(subscription.getNextBillingDate());
        history.setPaymentProvider(PaymentProvider.ASAAS);
        history.setExternalCustomerId(subscription.getExternalCustomerId());
        history.setExternalSubscriptionId(subscription.getExternalSubscriptionId());
        history.setExternalBillingId(newExternalBillingId);
        history.setExternalBillingStatus(newExternalBillingStatus);
        history.setNotes(buildHistoryNotes(billingType, previousExternalBillingId, previousExternalBillingStatus));
        storeSubscriptionBillingHistoryRepository.save(history);
    }

    private String buildHistoryNotes(
            String billingType,
            String previousExternalBillingId,
            String previousExternalBillingStatus
    ) {
        String notes = "Cobrança ASAAS gerada. type=" + billingType
                + ", prevBillingId=" + safeValue(previousExternalBillingId)
                + ", prevStatus=" + safeValue(previousExternalBillingStatus);

        if (notes.length() > MAX_HISTORY_NOTES_LENGTH) {
            return notes.substring(0, MAX_HISTORY_NOTES_LENGTH);
        }

        return notes;
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String normalizeBillingType(String billingType) {
        if (billingType == null || billingType.isBlank()) {
            throw new BusinessException("billingType é obrigatório");
        }

        String normalized = billingType.trim().toUpperCase();

        if (!ALLOWED_BILLING_TYPES.contains(normalized)) {
            throw new BusinessException("billingType inválido. Use apenas PIX, BOLETO ou CREDIT_CARD");
        }

        return normalized;
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String cleanNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}