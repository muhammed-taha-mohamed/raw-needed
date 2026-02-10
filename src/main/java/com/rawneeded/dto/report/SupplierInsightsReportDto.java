package com.rawneeded.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierInsightsReportDto {

    private List<MonthlyTrendPoint> monthlyTrend;
    private List<TopProductPoint> topProducts;
    private List<TopCustomerPoint> topCustomers;
    private List<StatusDistributionPoint> statusDistribution;
    private List<ValueBucketPoint> completedOrderValueBuckets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendPoint {
        private String monthKey; // YYYY-MM
        private Integer totalRequests;
        private Integer completedRequests;
        private Double totalSalesAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductPoint {
        private String productId;
        private String productName;
        private Integer requestedCount;
        private Double totalSalesAmount;
        private Map<String, Long> statusCounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomerPoint {
        private String customerKey;
        private String customerName;
        private Integer requestedCount;
        private Double totalSalesAmount;
        private Map<String, Long> statusCounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistributionPoint {
        private String status;
        private Integer count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueBucketPoint {
        private String bucketLabel;
        private Integer count;
        private Double totalSalesAmount;
    }
}
