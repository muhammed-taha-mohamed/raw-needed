package com.rawneeded.service.impl;

import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.dto.dashboard.MonthlyOrderStats;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.RFQOrder;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.repository.RFQOrderLineRepository;
import com.rawneeded.repository.RFQOrderRepository;
import com.rawneeded.service.ICustomerDashboardService;
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
public class CustomerDashboardServiceImpl implements ICustomerDashboardService {

    private final RFQOrderRepository orderRepository;
    private final RFQOrderLineRepository orderLineRepository;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;

    @Override
    public DashboardStatsDto getDashboardStats() {
        String token = messagesUtil.getAuthToken();
        String ownerId = tokenProvider.getOwnerIdFromToken(token);
        
        log.info("Getting dashboard stats for customer owner: {}", ownerId);
        
        // Get all orders for this customer
        List<RFQOrder> allOrders = orderRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        
        // Calculate basic statistics
        long totalOrders = allOrders.size();

        long pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW)
                .count();

        long negotiatingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.NEGOTIATING)
                .count();


        long completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();

        long underConfirmationOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.UNDER_CONFIRMATION)
                .count();

        long cancelledOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();


        
        // Get all order lines for this customer's orders
        List<String> orderIds = allOrders.stream().map(RFQOrder::getId).toList();
        List<RFQOrderLine> allOrderLines = new ArrayList<>();
        for (String orderId : orderIds) {
            allOrderLines.addAll(orderLineRepository.findByOrderId(orderId));
        }
        
        long totalOrderLines = allOrderLines.size();

        long pendingOrderLines = allOrderLines.stream()
                .filter(l -> l.getStatus() == LineStatus.PENDING)
                .count();
        long respondedOrderLines = allOrderLines.stream()
                .filter(l -> l.getStatus() == LineStatus.RESPONDED)
                .count();

        // Calculate monthly statistics
        List<MonthlyOrderStats> monthlyStats = calculateMonthlyStats(allOrders);
        
        // Calculate growth
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        
        long ordersThisMonth = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfThisMonth))
                .count();
        
        long ordersLastMonth = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && 
                        o.getCreatedAt().isAfter(startOfLastMonth) && 
                        o.getCreatedAt().isBefore(startOfThisMonth))
                .count();
        
        double monthlyGrowthPercentage = ordersLastMonth > 0 
                ? ((double)(ordersThisMonth - ordersLastMonth) / ordersLastMonth) * 100 
                : (ordersThisMonth > 0 ? 100.0 : 0.0);
        
        return DashboardStatsDto.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .underConfirmationOrders(underConfirmationOrders)
                .negotiatingOrders(negotiatingOrders)
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
    
    private List<MonthlyOrderStats> calculateMonthlyStats(List<RFQOrder> orders) {
        // Group orders by month
        Map<String, Long> ordersByMonth = orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        o -> {
                            LocalDateTime createdAt = o.getCreatedAt();
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
            
            // Calculate growth compared to previous month
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
                growthPercentage = 100.0; // First month with orders
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
