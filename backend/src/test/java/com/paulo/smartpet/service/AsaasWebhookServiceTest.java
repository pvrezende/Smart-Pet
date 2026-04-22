package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.asaas.AsaasWebhookEvent;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.PaymentProvider;
import com.paulo.smartpet.entity.SaasWebhookEventLog;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.StoreSubscriptionBillingHistory;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.repository.SaasWebhookEventLogRepository;
import com.paulo.smartpet.repository.StoreSubscriptionBillingHistoryRepository;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsaasWebhookServiceTest {

    @Mock
    private StoreSubscriptionRepository storeSubscriptionRepository;

    @Mock
    private StoreSubscriptionBillingHistoryRepository storeSubscriptionBillingHistoryRepository;

    @Mock
    private SaasWebhookEventLogRepository saasWebhookEventLogRepository;

    @Mock
    private SaasBillingService saasBillingService;

    @InjectMocks
    private AsaasWebhookService asaasWebhookService;

    private StoreSubscription subscription;

    @BeforeEach
    void setUp() {
        Store store = new Store();
        store.setId(2L);
        store.setName("Loja Centro");

        subscription = new StoreSubscription();
        subscription.setId(10L);
        subscription.setStore(store);
        subscription.setPlan(SubscriptionPlan.PRO);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setBillingStatus(BillingStatus.PENDING);
        subscription.setBillingDay(15);
        subscription.setNextBillingDate(LocalDate.of(2026, 5, 15));
        subscription.setMonthlyPrice(new BigDecimal("199.90"));
        subscription.setPaymentProvider(PaymentProvider.ASAAS);
        subscription.setExternalCustomerId("cus_123");
        subscription.setExternalSubscriptionId("sub_123");
        subscription.setExternalBillingId("pay_123");
        subscription.setExternalBillingStatus("PENDING");
    }

    @Test
    void shouldProcessPaymentReceivedAndAdvanceCycle() {
        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_RECEIVED",
                new AsaasWebhookEvent.Payment(
                        "pay_123",
                        "RECEIVED",
                        "PIX",
                        new BigDecimal("199.90"),
                        "2026-05-15",
                        "2026-05-20",
                        "Pagamento recebido",
                        "store-2-subscription-10",
                        "cus_123"
                )
        );

        when(saasWebhookEventLogRepository.existsByEventKey(anyString())).thenReturn(false);
        when(storeSubscriptionRepository.findAll()).thenReturn(List.of(subscription));
        when(saasBillingService.calculateNextCycleBillingDate(15, LocalDate.of(2026, 5, 20)))
                .thenReturn(LocalDate.of(2026, 6, 15));

        String result = asaasWebhookService.process(event);

        assertTrue(result.contains("PAYMENT_RECEIVED"));
        assertEquals(BillingStatus.PAID, subscription.getBillingStatus());
        assertEquals("RECEIVED", subscription.getExternalBillingStatus());
        assertEquals(LocalDate.of(2026, 6, 15), subscription.getNextBillingDate());

        verify(storeSubscriptionRepository).save(subscription);
        verify(storeSubscriptionBillingHistoryRepository).save(any(StoreSubscriptionBillingHistory.class));
        verify(saasWebhookEventLogRepository).save(any(SaasWebhookEventLog.class));
    }

    @Test
    void shouldIgnoreDuplicatedWebhookEvent() {
        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_RECEIVED",
                new AsaasWebhookEvent.Payment(
                        "pay_123",
                        "RECEIVED",
                        "PIX",
                        new BigDecimal("199.90"),
                        "2026-05-15",
                        "2026-05-20",
                        "Pagamento recebido",
                        "store-2-subscription-10",
                        "cus_123"
                )
        );

        when(saasWebhookEventLogRepository.existsByEventKey(anyString())).thenReturn(true);

        String result = asaasWebhookService.process(event);

        assertTrue(result.contains("já havia sido processado"));
        verify(storeSubscriptionRepository, never()).save(any());
        verify(storeSubscriptionBillingHistoryRepository, never()).save(any());
    }

    @Test
    void shouldSetOverdueWhenWebhookIsPaymentOverdue() {
        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_OVERDUE",
                new AsaasWebhookEvent.Payment(
                        "pay_123",
                        "OVERDUE",
                        "PIX",
                        new BigDecimal("199.90"),
                        "2026-05-15",
                        null,
                        "Pagamento vencido",
                        "store-2-subscription-10",
                        "cus_123"
                )
        );

        when(saasWebhookEventLogRepository.existsByEventKey(anyString())).thenReturn(false);
        when(storeSubscriptionRepository.findAll()).thenReturn(List.of(subscription));

        String result = asaasWebhookService.process(event);

        assertTrue(result.contains("PAYMENT_OVERDUE"));
        assertEquals(BillingStatus.OVERDUE, subscription.getBillingStatus());
        assertEquals("OVERDUE", subscription.getExternalBillingStatus());

        verify(storeSubscriptionRepository).save(subscription);
        verify(storeSubscriptionBillingHistoryRepository).save(any(StoreSubscriptionBillingHistory.class));
        verify(saasWebhookEventLogRepository).save(any(SaasWebhookEventLog.class));
    }

    @Test
    void shouldThrowWhenPaymentIsMissing() {
        AsaasWebhookEvent event = new AsaasWebhookEvent("PAYMENT_RECEIVED", null);

        BusinessException ex = assertThrows(BusinessException.class, () -> asaasWebhookService.process(event));

        assertEquals("Pagamento do webhook não informado", ex.getMessage());
    }

    @Test
    void shouldThrowWhenSubscriptionIsNotFound() {
        AsaasWebhookEvent event = new AsaasWebhookEvent(
                "PAYMENT_RECEIVED",
                new AsaasWebhookEvent.Payment(
                        "pay_not_found",
                        "RECEIVED",
                        "PIX",
                        new BigDecimal("199.90"),
                        "2026-05-15",
                        "2026-05-20",
                        "Pagamento recebido",
                        "store-2-subscription-10",
                        "cus_123"
                )
        );

        when(saasWebhookEventLogRepository.existsByEventKey(anyString())).thenReturn(false);
        when(storeSubscriptionRepository.findAll()).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, () -> asaasWebhookService.process(event));

        assertEquals("Assinatura não encontrada para o pagamento informado", ex.getMessage());
    }
}