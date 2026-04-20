package com.paulo.smartpet.dto;

public record StoreOnboardingResponse(
        StoreResponse store,
        UserResponse adminUser,
        StoreSubscriptionResponse subscription,
        String message
) {
}