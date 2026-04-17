package com.paulo.smartpet.dto;

import java.util.List;

public record ReportsAnalyticsResponse(
        List<ChartSliceResponse> salesByPaymentMethod,
        List<ChartSliceResponse> salesByStatus,
        List<LowStockItemResponse> lowStockProducts,
        List<StoreStockSummaryResponse> storeStockSummary
) {
}