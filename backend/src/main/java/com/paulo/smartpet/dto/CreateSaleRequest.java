package com.paulo.smartpet.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateSaleRequest(Long customerId, List<SaleItemRequest> items, String paymentMethod, BigDecimal discount, String notes) {
}
