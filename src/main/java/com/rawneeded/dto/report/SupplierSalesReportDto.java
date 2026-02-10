package com.rawneeded.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSalesReportDto {
    private Integer totalRequestsCount;
    private Integer completedRequestsCount;
    private Double totalSalesAmount;
    private Map<String, Long> overallStatusCounts;
    private TopProduct topProduct;
    private TopCustomer topCustomer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private String productId;
        private String productName;
        private Integer requestedCount;
        private Map<String, Long> statusCounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private String customerOwnerId;
        private String customerName;
        private String customerOrganizationName;
        private Integer requestedCount;
        private Map<String, Long> statusCounts;
    }
}
