package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaasFinancialDashboardResponse;
import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionPlan;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.repository.StoreSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaasFinancialDashboardServiceTest {

    @Mock
    private StoreSubscriptionRepository storeSubscriptionRepository;

    @InjectMocks
    private SaasFinancialDashboardService saasFinancialDashboardService;

    private Store store1;
    private Store store2;
    private Store store3;

    @BeforeEach
    void setUp() {
        store1 = new Store();
        store1.setId(1L);
        store1.setName("Loja Centro");

        store2 = new Store();
        store2.setId(2L);
        store2.setName("Loja Norte");

        store3 = new Store();
        store3.setId(3L);
        store3.setName("Loja Sul");
    }

    @Test
    void shouldReturnEmptyDashboardWhenThereAreNoSubscriptions() {
        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        SaasFinancialDashboardResponse response = saasFinancialDashboardService.getDashboard();

        assertEquals(0, response.totalStores());
        assertEquals(0, response.totalSubscriptions());
        assertEquals(0, response.activeSubscriptions());
        assertEquals(0, response.trialSubscriptions());
        assertEquals(0, response.overdueSubscriptions());
        assertEquals(0, response.canceledSubscriptions());
        assertEquals(new BigDecimal("0"), response.estimatedMonthlyRevenue());
        assertEquals(new BigDecimal("0"), response.activeMonthlyRevenue());
        assertEquals(new BigDecimal("0"), response.overdueMonthlyRevenue());
        assertEquals(new BigDecimal("0"), response.averageTicket());
    }

    @Test
    void shouldCalculateDashboardMetricsCorrectly() {
        StoreSubscription sub1 = buildSubscription(
                store1,
                SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PAID,
                new BigDecimal("199.90")
        );

        StoreSubscription sub2 = buildSubscription(
                store2,
                SubscriptionPlan.BASIC,
                SubscriptionStatus.TRIAL,
                BillingStatus.TRIAL,
                new BigDecimal("99.90")
        );

        StoreSubscription sub3 = buildSubscription(
                store3,
                SubscriptionPlan.ENTERPRISE,
                SubscriptionStatus.CANCELED,
                BillingStatus.OVERDUE,
                new BigDecimal("499.90")
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(sub1, sub2, sub3));

        SaasFinancialDashboardResponse response = saasFinancialDashboardService.getDashboard();

        assertEquals(3, response.totalStores());
        assertEquals(3, response.totalSubscriptions());
        assertEquals(1, response.activeSubscriptions());
        assertEquals(1, response.trialSubscriptions());
        assertEquals(1, response.overdueSubscriptions());
        assertEquals(1, response.canceledSubscriptions());

        assertEquals(new BigDecimal("799.70"), response.estimatedMonthlyRevenue());
        assertEquals(new BigDecimal("199.90"), response.activeMonthlyRevenue());
        assertEquals(new BigDecimal("499.90"), response.overdueMonthlyRevenue());
        assertEquals(new BigDecimal("266.57"), response.averageTicket());

        assertEquals(1L, response.subscriptionsByPlan().get(SubscriptionPlan.BASIC));
        assertEquals(1L, response.subscriptionsByPlan().get(SubscriptionPlan.PRO));
        assertEquals(1L, response.subscriptionsByPlan().get(SubscriptionPlan.ENTERPRISE));

        assertEquals(1L, response.subscriptionsByStatus().get(SubscriptionStatus.ACTIVE));
        assertEquals(1L, response.subscriptionsByStatus().get(SubscriptionStatus.TRIAL));
        assertEquals(1L, response.subscriptionsByStatus().get(SubscriptionStatus.CANCELED));

        assertEquals(1L, response.subscriptionsByBillingStatus().get(BillingStatus.PAID));
        assertEquals(1L, response.subscriptionsByBillingStatus().get(BillingStatus.TRIAL));
        assertEquals(1L, response.subscriptionsByBillingStatus().get(BillingStatus.OVERDUE));
    }

    @Test
    void shouldCountDistinctStoresOnlyOnce() {
        StoreSubscription sub1 = buildSubscription(
                store1,
                SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PAID,
                new BigDecimal("199.90")
        );

        StoreSubscription sub2 = buildSubscription(
                store1,
                SubscriptionPlan.BASIC,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PENDING,
                new BigDecimal("99.90")
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(sub1, sub2));

        SaasFinancialDashboardResponse response = saasFinancialDashboardService.getDashboard();

        assertEquals(1, response.totalStores());
        assertEquals(2, response.totalSubscriptions());
        assertEquals(new BigDecimal("299.80"), response.estimatedMonthlyRevenue());
        assertEquals(new BigDecimal("299.80"), response.activeMonthlyRevenue());
        assertEquals(new BigDecimal("149.90"), response.averageTicket());
    }

    @Test
    void shouldTreatNullPriceAsZero() {
        StoreSubscription sub1 = buildSubscription(
                store1,
                SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE,
                BillingStatus.PAID,
                null
        );

        when(storeSubscriptionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(sub1));

        SaasFinancialDashboardResponse response = saasFinancialDashboardService.getDashboard();

        assertEquals(new BigDecimal("0"), response.estimatedMonthlyRevenue());
        assertEquals(new BigDecimal("0"), response.activeMonthlyRevenue());
        assertEquals(new BigDecimal("0.00"), response.averageTicket());
    }

    private StoreSubscription buildSubscription(
            Store store,
            SubscriptionPlan plan,
            SubscriptionStatus subscriptionStatus,
            BillingStatus billingStatus,
            BigDecimal monthlyPrice
    ) {
        StoreSubscription subscription = new StoreSubscription();
        subscription.setStore(store);
        subscription.setPlan(plan);
        subscription.setStatus(subscriptionStatus);
        subscription.setBillingStatus(billingStatus);
        subscription.setMonthlyPrice(monthlyPrice);
        return subscription;
    }
}