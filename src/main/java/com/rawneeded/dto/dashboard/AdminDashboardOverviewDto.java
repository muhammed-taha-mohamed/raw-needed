package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Single comprehensive payload for admin dashboard: stats, charts, and reports.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardOverviewDto {

    // ——— Core stats (existing) ———
    private SubscriptionSummaryDto subscriptionSummary;
    private UserStatsDto userStats;
    private AdSubscriptionStatsDto adSubscriptionStats;
    private PendingCountsDto pendingCounts;
    private DashboardStatsDto dashboardStats;

    // ——— Chart data ———
    /** Orders count per month (last 12 months) */
    private List<TimeSeriesPointDto> ordersOverTime;
    /** New subscriptions count per month (last 12 months) */
    private List<TimeSeriesPointDto> subscriptionsOverTime;
    /** Order count by status (NEW, NEGOTIATING, COMPLETED, CANCELLED, etc.) */
    private List<PieSliceDto> ordersByStatus;
    /** User count by role (suppliers, customers, admins) */
    private List<PieSliceDto> usersByRole;
    /** Subscription revenue per month (last 12 months) – sum of approved subscription finalPrice */
    private List<TimeSeriesPointDto> revenueOverTime;

    // ——— Report / list data ———
    private List<RecentOrderSummaryDto> recentOrders;
    private List<RecentComplaintSummaryDto> recentComplaints;

    // ——— Extra counts for cards ———
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
