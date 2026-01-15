package com.rawneeded.service.impl;

import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.dto.dashboard.MonthlyOrderStats;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.repository.RFQOrderLineRepository;
import com.rawneeded.repository.RFQOrderRepository;
import com.rawneeded.service.ISupplierDashboardService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class SupplierDashboardServiceImpl implements ISupplierDashboardService {

    private final RFQOrderRepository orderRepository;
    private final RFQOrderLineRepository orderLineRepository;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;

    @Override
    public DashboardStatsDto getDashboardStats() {
        String token = messagesUtil.getAuthToken();
        String ownerId = tokenProvider.getOwnerIdFromToken(token);
        
        log.info("Getting dashboard stats for supplier owner: {}", ownerId);
        
        // Get all order lines for this supplier
        List<RFQOrderLine> allOrderLines = orderLineRepository.findBySupplierId(ownerId);
        
        // Get unique order IDs from order lines
        List<String> orderIds = allOrderLines.stream()
                .map(RFQOrderLine::getOrderId)
                .distinct()
                .toList();
        
        // Calculate basic statistics
        long totalOrderLines = allOrderLines.size();
        long pendingOrderLines = allOrderLines.stream()
                .filter(l -> l.getStatus() == LineStatus.PENDING)
                .count();
        long respondedOrderLines = allOrderLines.stream()
                .filter(l -> l.getStatus() == LineStatus.RESPONDED)
                .count();
        
        // For supplier, "orders" means order lines
        long totalOrders = totalOrderLines;
        long pendingOrders = pendingOrderLines;
        long sentOrders = totalOrderLines; // All order lines sent to supplier
        long completedOrders = respondedOrderLines;
        long cancelledOrders = 0; // Suppliers don't cancel, customers do
        
        // Calculate monthly statistics based on order lines
        // We need to get the orders to get creation dates
        List<com.rawneeded.model.RFQOrder> orders = new ArrayList<>();
        for (String orderId : orderIds) {
            orderRepository.findById(orderId).ifPresent(orders::add);
        }
        
        List<MonthlyOrderStats> monthlyStats = calculateMonthlyStatsForSupplier(allOrderLines, orders);
        
        // Calculate growth
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        
        long ordersThisMonth = allOrderLines.stream()
                .filter(l -> {
                    com.rawneeded.model.RFQOrder order = orders.stream()
                            .filter(o -> o.getId().equals(l.getOrderId()))
                            .findFirst()
                            .orElse(null);
                    return order != null && order.getCreatedAt() != null && 
                           order.getCreatedAt().isAfter(startOfThisMonth);
                })
                .count();
        
        long ordersLastMonth = allOrderLines.stream()
                .filter(l -> {
                    com.rawneeded.model.RFQOrder order = orders.stream()
                            .filter(o -> o.getId().equals(l.getOrderId()))
                            .findFirst()
                            .orElse(null);
                    return order != null && order.getCreatedAt() != null && 
                           order.getCreatedAt().isAfter(startOfLastMonth) && 
                           order.getCreatedAt().isBefore(startOfThisMonth);
                })
                .count();
        
        double monthlyGrowthPercentage = ordersLastMonth > 0 
                ? ((double)(ordersThisMonth - ordersLastMonth) / ordersLastMonth) * 100 
                : (ordersThisMonth > 0 ? 100.0 : 0.0);
        
        return DashboardStatsDto.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .sentOrders(sentOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .monthlyStats(monthlyStats)
                .monthlyGrowthPercentage(monthlyGrowthPercentage)
                .ordersThisMonth(ordersThisMonth)
                .ordersLastMonth(ordersLastMonth)
                .totalOrderLines(totalOrderLines)
                .pendingOrderLines(pendingOrderLines)
                .respondedOrderLines(respondedOrderLines)
                .build();
    }
    
    private List<MonthlyOrderStats> calculateMonthlyStatsForSupplier(
            List<RFQOrderLine> orderLines, 
            List<com.rawneeded.model.RFQOrder> orders) {
        
        // Create a map of orderId to order for quick lookup
        Map<String, com.rawneeded.model.RFQOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(com.rawneeded.model.RFQOrder::getId, o -> o));
        
        // Group order lines by month of their parent order
        Map<String, Long> ordersByMonth = orderLines.stream()
                .filter(l -> {
                    com.rawneeded.model.RFQOrder order = orderMap.get(l.getOrderId());
                    return order != null && order.getCreatedAt() != null;
                })
                .collect(Collectors.groupingBy(
                        l -> {
                            com.rawneeded.model.RFQOrder order = orderMap.get(l.getOrderId());
                            LocalDateTime createdAt = order.getCreatedAt();
                            Month month = createdAt.getMonth();
                            int year = createdAt.getYear();
                            return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
                        },
                        Collectors.counting()
                ));
        
        List<MonthlyOrderStats> monthlyStats = new ArrayList<>();
        List<String> sortedMonths = ordersByMonth.keySet().stream()
                .sorted()
                .toList();
        
        for (int i = 0; i < sortedMonths.size(); i++) {
            String month = sortedMonths.get(i);
            long count = ordersByMonth.get(month);
            
            double growthPercentage = 0.0;
            if (i > 0) {
                String previousMonth = sortedMonths.get(i - 1);
                long previousCount = ordersByMonth.get(previousMonth);
                if (previousCount > 0) {
                    growthPercentage = ((double)(count - previousCount) / previousCount) * 100;
                } else if (count > 0) {
                    growthPercentage = 100.0;
                }
            } else if (count > 0) {
                growthPercentage = 100.0;
            }
            
            monthlyStats.add(MonthlyOrderStats.builder()
                    .month(month)
                    .orderCount(count)
                    .growthPercentage(growthPercentage)
                    .build());
        }
        
        return monthlyStats;
    }
}
