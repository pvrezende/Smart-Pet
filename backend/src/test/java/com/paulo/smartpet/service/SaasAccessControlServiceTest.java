package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.BillingStatus;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.entity.StoreSubscription;
import com.paulo.smartpet.entity.SubscriptionStatus;
import com.paulo.smartpet.entity.User;
import com.paulo.smartpet.entity.UserRole;
import com.paulo.smartpet.exception.BillingAccessDeniedException;
import com.paulo.smartpet.exception.SaasAccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaasAccessControlServiceTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private StoreSubscriptionService storeSubscriptionService;

    @InjectMocks
    private SaasAccessControlService saasAccessControlService;

    private User adminStoreUser;
    private StoreSubscription subscription;

    @BeforeEach
    void setUp() {
        Store store = new Store();
        store.setId(2L);
        store.setName("Loja Centro");

        adminStoreUser = new User();
        adminStoreUser.setId(10L);
        adminStoreUser.setName("Maria Gestora");
        adminStoreUser.setUsername("maria.centro");
        adminStoreUser.setRole(UserRole.ADMIN_STORE);
        adminStoreUser.setStore(store);

        subscription = new StoreSubscription();
        subscription.setId(100L);
        subscription.setStore(store);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setBillingStatus(BillingStatus.PAID);
    }

    @Test
    void shouldAllowAccessForSuperAdmin() {
        User superAdmin = new User();
        superAdmin.setId(1L);
        superAdmin.setRole(UserRole.SUPER_ADMIN);

        when(authenticatedUserService.getCurrentUser()).thenReturn(superAdmin);

        assertDoesNotThrow(() -> saasAccessControlService.validateCurrentUserOperationalAccess());
    }

    @Test
    void shouldAllowAccessForAdmin() {
        User admin = new User();
        admin.setId(2L);
        admin.setRole(UserRole.ADMIN);

        when(authenticatedUserService.getCurrentUser()).thenReturn(admin);

        assertDoesNotThrow(() -> saasAccessControlService.validateCurrentUserOperationalAccess());
    }

    @Test
    void shouldAllowAccessForActiveStoreWithPaidBilling() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(adminStoreUser);
        when(authenticatedUserService.getRequiredStoreId(adminStoreUser)).thenReturn(2L);
        when(storeSubscriptionService.getEntityByStoreId(2L)).thenReturn(subscription);

        assertDoesNotThrow(() -> saasAccessControlService.validateCurrentUserOperationalAccess());
    }

    @Test
    void shouldBlockAccessWhenBillingIsOverdue() {
        subscription.setBillingStatus(BillingStatus.OVERDUE);

        when(authenticatedUserService.getCurrentUser()).thenReturn(adminStoreUser);
        when(authenticatedUserService.getRequiredStoreId(adminStoreUser)).thenReturn(2L);
        when(storeSubscriptionService.getEntityByStoreId(2L)).thenReturn(subscription);

        BillingAccessDeniedException ex = assertThrows(
                BillingAccessDeniedException.class,
                () -> saasAccessControlService.validateCurrentUserOperationalAccess()
        );

        assertEquals("Acesso bloqueado: loja com cobrança em atraso", ex.getMessage());
    }

    @Test
    void shouldBlockAccessWhenSubscriptionIsSuspended() {
        subscription.setStatus(SubscriptionStatus.SUSPENDED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(adminStoreUser);
        when(authenticatedUserService.getRequiredStoreId(adminStoreUser)).thenReturn(2L);
        when(storeSubscriptionService.getEntityByStoreId(2L)).thenReturn(subscription);

        SaasAccessDeniedException ex = assertThrows(
                SaasAccessDeniedException.class,
                () -> saasAccessControlService.validateCurrentUserOperationalAccess()
        );

        assertEquals("Acesso bloqueado: assinatura da loja está suspensa", ex.getMessage());
    }

    @Test
    void shouldBlockAccessWhenSubscriptionIsCanceled() {
        subscription.setStatus(SubscriptionStatus.CANCELED);

        when(authenticatedUserService.getCurrentUser()).thenReturn(adminStoreUser);
        when(authenticatedUserService.getRequiredStoreId(adminStoreUser)).thenReturn(2L);
        when(storeSubscriptionService.getEntityByStoreId(2L)).thenReturn(subscription);

        SaasAccessDeniedException ex = assertThrows(
                SaasAccessDeniedException.class,
                () -> saasAccessControlService.validateCurrentUserOperationalAccess()
        );

        assertEquals("Acesso bloqueado: assinatura da loja está cancelada", ex.getMessage());
    }

    @Test
    void shouldAllowAccessWhenTrialIsStillValid() {
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setTrialEndsAt(LocalDateTime.now().plusDays(2));

        when(authenticatedUserService.getCurrentUser()).thenReturn(adminStoreUser);
        when(authenticatedUserService.getRequiredStoreId(adminStoreUser)).thenReturn(2L);
        when(storeSubscriptionService.getEntityByStoreId(2L)).thenReturn(subscription);

        assertDoesNotThrow(() -> saasAccessControlService.validateCurrentUserOperationalAccess());
    }

    @Test
    void shouldBlockAccessWhenTrialIsExpired() {
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setTrialEndsAt(LocalDateTime.now().minusMinutes(1));

        when(authenticatedUserService.getCurrentUser()).thenReturn(adminStoreUser);
        when(authenticatedUserService.getRequiredStoreId(adminStoreUser)).thenReturn(2L);
        when(storeSubscriptionService.getEntityByStoreId(2L)).thenReturn(subscription);

        SaasAccessDeniedException ex = assertThrows(
                SaasAccessDeniedException.class,
                () -> saasAccessControlService.validateCurrentUserOperationalAccess()
        );

        assertEquals("Acesso bloqueado: período de trial da loja expirou", ex.getMessage());
    }
}