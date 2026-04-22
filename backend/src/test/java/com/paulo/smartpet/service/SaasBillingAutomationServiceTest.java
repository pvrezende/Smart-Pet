package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasBillingAutomationResponse;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaasBillingAutomationServiceTest {

    @Mock
    private StoreSubscriptionRepository storeSubscriptionRepository;

    @Mock
    private StoreSubscriptionPaymentService storeSubscriptionPaymentService;

    @InjectMocks
    private SaasBillingAutomationService saasBillingAutomationService;

    private Store store;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(2L);
        store.setName("Loja Centro");
    }

    @Test
    void shouldGenerateChargeForEligibleSubscription() {
        StoreSubscription eligible = buildSubscription(
                1L,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PENDING,
                LocalDate.now().minusDays(1)
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(eligible));

        SaasBillingAutomationResponse response = saasBillingAutomationService.executeAutomaticChargeGeneration();

        assertEquals(1, response.totalSubscriptionsChecked());
        assertEquals(1, response.totalChargesGenerated());
        assertEquals(0, response.totalSkipped());

        verify(storeSubscriptionPaymentService).generatePaymentLink(2L, "PIX");
    }

    @Test
    void shouldSkipSubscriptionWhenNextBillingDateIsInFuture() {
        StoreSubscription future = buildSubscription(
                1L,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PENDING,
                LocalDate.now().plusDays(5)
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(future));

        SaasBillingAutomationResponse response = saasBillingAutomationService.executeAutomaticChargeGeneration();

        assertEquals(1, response.totalSubscriptionsChecked());
        assertEquals(0, response.totalChargesGenerated());
        assertEquals(1, response.totalSkipped());

        verify(storeSubscriptionPaymentService, never()).generatePaymentLink(anyLong(), anyString());
    }

    @Test
    void shouldSkipSubscriptionWhenStatusIsNotActive() {
        StoreSubscription trial = buildSubscription(
                1L,
                SubscriptionStatus.TRIAL,
                BillingStatus.TRIAL,
                LocalDate.now().minusDays(1)
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(trial));

        SaasBillingAutomationResponse response = saasBillingAutomationService.executeAutomaticChargeGeneration();

        assertEquals(1, response.totalSubscriptionsChecked());
        assertEquals(0, response.totalChargesGenerated());
        assertEquals(1, response.totalSkipped());

        verify(storeSubscriptionPaymentService, never()).generatePaymentLink(anyLong(), anyString());
    }

    @Test
    void shouldSkipSubscriptionWhenBillingIsCanceled() {
        StoreSubscription canceledBilling = buildSubscription(
                1L,
                SubscriptionStatus.ACTIVE,
                BillingStatus.CANCELED,
                LocalDate.now().minusDays(1)
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(canceledBilling));

        SaasBillingAutomationResponse response = saasBillingAutomationService.executeAutomaticChargeGeneration();

        assertEquals(1, response.totalSubscriptionsChecked());
        assertEquals(0, response.totalChargesGenerated());
        assertEquals(1, response.totalSkipped());

        verify(storeSubscriptionPaymentService, never()).generatePaymentLink(anyLong(), anyString());
    }

    @Test
    void shouldSkipWhenChargeGenerationThrowsError() {
        StoreSubscription eligible = buildSubscription(
                1L,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PENDING,
                LocalDate.now().minusDays(1)
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(eligible));
        doThrow(new RuntimeException("Erro ASAAS")).when(storeSubscriptionPaymentService)
                .generatePaymentLink(2L, "PIX");

        SaasBillingAutomationResponse response = saasBillingAutomationService.executeAutomaticChargeGeneration();

        assertEquals(1, response.totalSubscriptionsChecked());
        assertEquals(0, response.totalChargesGenerated());
        assertEquals(1, response.totalSkipped());

        verify(storeSubscriptionPaymentService).generatePaymentLink(2L, "PIX");
    }

    @Test
    void shouldHandleMultipleSubscriptions() {
        StoreSubscription eligible = buildSubscription(
                1L,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PENDING,
                LocalDate.now().minusDays(1)
        );

        StoreSubscription future = buildSubscription(
                2L,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PENDING,
                LocalDate.now().plusDays(3)
        );

        StoreSubscription overdue = buildSubscription(
                3L,
                SubscriptionStatus.ACTIVE,
                BillingStatus.OVERDUE,
                LocalDate.now()
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(eligible, future, overdue));

        SaasBillingAutomationResponse response = saasBillingAutomationService.executeAutomaticChargeGeneration();

        assertEquals(3, response.totalSubscriptionsChecked());
        assertEquals(2, response.totalChargesGenerated());
        assertEquals(1, response.totalSkipped());

        verify(storeSubscriptionPaymentService, times(2)).generatePaymentLink(2L, "PIX");
    }

    private StoreSubscription buildSubscription(
            Long id,
            SubscriptionStatus subscriptionStatus,
            BillingStatus billingStatus,
            LocalDate nextBillingDate
    ) {
        StoreSubscription subscription = new StoreSubscription();
        subscription.setId(id);
        subscription.setStore(store);
        subscription.setStatus(subscriptionStatus);
        subscription.setBillingStatus(billingStatus);
        subscription.setNextBillingDate(nextBillingDate);
        return subscription;
    }
}