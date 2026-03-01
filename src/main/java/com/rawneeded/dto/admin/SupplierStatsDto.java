package com.rawneeded.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierStatsDto {
    private long totalProducts;
    private long inStockProducts;
    private long totalOrderLines;
    private long distinctOrders;
    /** Line status -> count (PENDING, RESPONDED, APPROVED, REJECTED, COMPLETED) */
    private Map<String, Long> orderLinesByStatus;
}
