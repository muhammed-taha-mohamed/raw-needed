package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComprehensiveDashboardStatsDto {
    
    // User Statistics
    private long totalUsers;
    private long totalSuppliers;
    private long totalCustomers;
    private long activeUsers;
    private long inactiveUsers;
    
    // Order Statistics
    private long totalOrders;
    private long pendingOrders;
    private long sentOrders;
    private long completedOrders;
    private long cancelledOrders;
    
    // Order Line Statistics
    private long totalOrderLines;
    private long pendingOrderLines;
    private long respondedOrderLines;
    private long rejectedOrderLines;
    
    // Product Statistics
    private long totalProducts;
    private long inStockProducts;
    private long outOfStockProducts;
    
    // Category Statistics
    private long totalCategories;
    private long totalSubCategories;
    
    // Cart Statistics
    private long totalCarts;
    private long cartsWithItems;
    
    // Quotation Statistics
    private long totalQuotations;
    
    // Subscription Statistics
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long pendingSubscriptions;
    private long totalSubscriptionPlans;
    
    // Notification Statistics
    private long totalNotifications;
    private long unreadNotifications;
    private long readNotifications;
    
    // Monthly Statistics
    private List<MonthlyOrderStats> monthlyOrderStats;
    
    // Growth Statistics
    private double monthlyOrderGrowthPercentage;
    private long ordersThisMonth;
    private long ordersLastMonth;
    
    // Additional Statistics
    private long totalOrganizations;
    private long suppliersWithProducts;
    private long customersWithOrders;
}
