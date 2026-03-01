package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardCountsDto {
    private long totalProducts;
    private long totalCategories;
    private long totalSubCategories;
    private long totalComplaints;
    private long openComplaints;
    private long totalPosts;
    private long totalOffers;
    private long totalNotifications;
    private double totalSubscriptionRevenue;
    private double subscriptionRevenueThisMonth;
    private long addSearchesPending;
}
