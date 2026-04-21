package com.paulo.smartpet.dto;

import java.math.BigDecimal;

public record SaasPlanResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal monthlyPrice,
        Boolean active,
        Boolean highlighted,
        Integer displayOrder
) {
}