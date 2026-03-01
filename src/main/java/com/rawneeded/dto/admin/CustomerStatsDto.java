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
public class CustomerStatsDto {
    private long totalOrders;
    /** Order status -> count (NEW, NEGOTIATING, UNDER_CONFIRMATION, COMPLETED, CANCELLED) */
    private Map<String, Long> ordersByStatus;
}
