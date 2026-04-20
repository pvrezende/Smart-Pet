package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.SubscriptionPlan;
import org.springframework.stereotype.Service;

@Service
public class SaasPlanLimitService {

    public int getUsersLimit(SubscriptionPlan plan) {
        if (plan == null) {
            return 0;
        }

        return switch (plan) {
            case BASIC -> 2;
            case PRO -> 5;
            case ENTERPRISE -> 999;
        };
    }

    public boolean canCreateUsers(SubscriptionPlan plan, long currentActiveUsers) {
        return currentActiveUsers < getUsersLimit(plan);
    }
}