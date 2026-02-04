package com.rawneeded.dto.dashboard;

import com.rawneeded.dto.RFQ.RFQOrderResponseDto;
import com.rawneeded.dto.user.SupplierInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    // Overall Statistics
    private long totalOrders;
    private long pendingOrders;
    private long negotiatingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long underConfirmationOrders;


    private long sentOrders;

    // Monthly Statistics
    private List<MonthlyOrderStats> monthlyStats;
    
    // Growth Statistics
    private double monthlyGrowthPercentage;
    private long ordersThisMonth;
    private long ordersLastMonth;
    
    // Additional stats
    private long totalOrderLines;
    private long pendingOrderLines;
    private long respondedOrderLines;
    
    // Latest Order
    private RFQOrderResponseDto latestOrder;
    
    // Most Requested Supplier
    private SupplierInfo mostRequestedSupplier;
    private long mostRequestedSupplierOrderCount;
    
    // Screen-specific statistics
    private long cartItemsCount;
    private long vendorsCount;
    private long productsCount;
    private long marketRequestsCount;
    private long teamMembersCount;
    private long complaintsCount;
}
