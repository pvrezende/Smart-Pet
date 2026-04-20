package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.SaasFeature;
import com.paulo.smartpet.entity.SubscriptionPlan;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SaasPlanFeatureService {

    public boolean hasFeature(SubscriptionPlan plan, SaasFeature feature) {
        if (plan == null || feature == null) {
            return false;
        }

        return switch (feature) {
            case ADVANCED_ANALYTICS -> plan == SubscriptionPlan.PRO || plan == SubscriptionPlan.ENTERPRISE;
        };
    }

    public List<SaasFeature> getEnabledFeatures(SubscriptionPlan plan) {
        return Arrays.stream(SaasFeature.values())
                .filter(feature -> hasFeature(plan, feature))
                .toList();
    }

    public List<SaasFeature> getDisabledFeatures(SubscriptionPlan plan) {
        return Arrays.stream(SaasFeature.values())
                .filter(feature -> !hasFeature(plan, feature))
                .toList();
    }
}