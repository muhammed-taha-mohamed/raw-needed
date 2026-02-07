package com.rawneeded.service.impl;

import com.rawneeded.dto.dashboard.AdSubscriptionStatsDto;
import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.dto.dashboard.MonthlyOrderStats;
import com.rawneeded.dto.dashboard.PendingCountsDto;
import com.rawneeded.dto.dashboard.SubscriptionSummaryDto;
import com.rawneeded.dto.dashboard.UserStatsDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserSubscriptionMapper;
import com.rawneeded.model.RFQOrder;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.model.AdSubscription;
import com.rawneeded.repository.*;
import com.rawneeded.service.IAdminDashboardService;
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
public class AdminDashboardServiceImpl implements IAdminDashboardService {

    private final RFQOrderRepository orderRepository;
    private final RFQOrderLineRepository orderLineRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final AdSubscriptionRepository adSubscriptionRepository;
    private final AddSearchesRequestRepository addSearchesRequestRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;

    @Override
    public DashboardStatsDto getDashboardStats() {
        String token = messagesUtil.getAuthToken();
        String userId = tokenProvider.getIdFromToken(token);
        
        log.info("Getting dashboard stats for admin user: {}", userId);
        
        // Get all orders in the system
        List<RFQOrder> allOrders = orderRepository.findAll();
        
        // Calculate basic statistics
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW || o.getStatus() == OrderStatus.NEGOTIATING)
                .count();
        long sentOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW || o.getStatus() == OrderStatus.NEGOTIATING || o.getStatus() == OrderStatus.COMPLETED)
                .count();
        long completedOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();
        long cancelledOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();
        
        // Get all order lines in the system
        List<RFQOrderLine> allOrderLines = orderLineRepository.findAll();
        
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
        
        // User Statistics (count queries - no full load)
        long totalUsers = userRepository.count();
        long totalSuppliers = userRepository.countByRole(Role.SUPPLIER_OWNER);
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER_OWNER);
        
        // Subscription Statistics (count queries)
        long totalSubscriptions = userSubscriptionRepository.count();
        long activeSubscriptions = userSubscriptionRepository.countByStatusAndExpiryDateAfter(UserSubscriptionStatus.APPROVED, LocalDateTime.now());
        long pendingSubscriptions = userSubscriptionRepository.countByStatus(UserSubscriptionStatus.PENDING);
        long subscriptionsThisMonth = userSubscriptionRepository.countBySubmissionDateAfter(startOfThisMonth);
        
        // Historical Subscriptions: load only last 2000 by submission date to avoid heavy queries
        List<UserSubscriptionResponseDto> historicalSubscriptions = new ArrayList<>();
        org.springframework.data.domain.Page<UserSubscription> recentPage = userSubscriptionRepository.findAllByOrderBySubmissionDateDesc(org.springframework.data.domain.PageRequest.of(0, 2000));
        Map<String, List<UserSubscription>> subscriptionsByUser = recentPage.getContent().stream()
                .filter(s -> s.getUserId() != null)
                .collect(Collectors.groupingBy(UserSubscription::getUserId));
        
        for (Map.Entry<String, List<UserSubscription>> entry : subscriptionsByUser.entrySet()) {
            List<UserSubscription> userSubs = entry.getValue();
            if (userSubs.size() > 1) {
                userSubs.sort((a, b) -> {
                    LocalDateTime dateA = a.getSubmissionDate() != null ? a.getSubmissionDate() : LocalDateTime.MIN;
                    LocalDateTime dateB = b.getSubmissionDate() != null ? b.getSubmissionDate() : LocalDateTime.MIN;
                    return dateB.compareTo(dateA);
                });
                for (int i = 1; i < userSubs.size(); i++) {
                    historicalSubscriptions.add(userSubscriptionMapper.toResponseDto(userSubs.get(i)));
                }
            }
        }
        
        // Ad Subscription Statistics (count queries)
        long totalAdSubscriptions = adSubscriptionRepository.count();
        long activeAdSubscriptions = adSubscriptionRepository.countByStatusAndEndDateAfterAndRemainingAdsGreaterThan(
                UserSubscriptionStatus.APPROVED, LocalDateTime.now(), 0);
        long pendingAdSubscriptions = adSubscriptionRepository.countByStatus(UserSubscriptionStatus.PENDING);
        long adSubscriptionsThisMonth = adSubscriptionRepository.countByRequestedAtAfter(startOfThisMonth);

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
                .totalUsers(totalUsers)
                .totalSuppliers(totalSuppliers)
                .totalCustomers(totalCustomers)
                .totalSubscriptions(totalSubscriptions)
                .activeSubscriptions(activeSubscriptions)
                .pendingSubscriptions(pendingSubscriptions)
                .subscriptionsThisMonth(subscriptionsThisMonth)
                .totalAdSubscriptions(totalAdSubscriptions)
                .activeAdSubscriptions(activeAdSubscriptions)
                .pendingAdSubscriptions(pendingAdSubscriptions)
                .adSubscriptionsThisMonth(adSubscriptionsThisMonth)
                .historicalSubscriptions(historicalSubscriptions)
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

    @Override
    public SubscriptionSummaryDto getSubscriptionSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long total = userSubscriptionRepository.count();
        long active = userSubscriptionRepository.countByStatusAndExpiryDateAfter(UserSubscriptionStatus.APPROVED, now);
        long pending = userSubscriptionRepository.countByStatus(UserSubscriptionStatus.PENDING);
        long thisMonth = userSubscriptionRepository.countBySubmissionDateAfter(startOfThisMonth);
        return SubscriptionSummaryDto.builder()
                .totalSubscriptions(total)
                .activeSubscriptions(active)
                .pendingSubscriptions(pending)
                .subscriptionsThisMonth(thisMonth)
                .build();
    }

    @Override
    public UserStatsDto getUserStats() {
        long total = userRepository.count();
        long suppliers = userRepository.countByRole(Role.SUPPLIER_OWNER);
        long customers = userRepository.countByRole(Role.CUSTOMER_OWNER);
        return UserStatsDto.builder()
                .totalUsers(total)
                .totalSuppliers(suppliers)
                .totalCustomers(customers)
                .build();
    }

    @Override
    public AdSubscriptionStatsDto getAdSubscriptionStats() {
        long total = adSubscriptionRepository.count();
        long active = adSubscriptionRepository.countByStatusAndRemainingAdsGreaterThan(
                UserSubscriptionStatus.APPROVED, 0);
        long pending = adSubscriptionRepository.countByStatus(UserSubscriptionStatus.PENDING);
        return AdSubscriptionStatsDto.builder()
                .totalAdSubscriptions(total)
                .activeAdSubscriptions(active)
                .pendingAdSubscriptions(pending)
                .build();
    }

    @Override
    public PendingCountsDto getPendingCounts() {
        long pendingSubscriptions = userSubscriptionRepository.countByStatus(UserSubscriptionStatus.PENDING);
        long pendingAdSubscriptions = adSubscriptionRepository.countByStatus(UserSubscriptionStatus.PENDING);
        long pendingAddSearches = addSearchesRequestRepository.countByStatus(UserSubscriptionStatus.PENDING);
        return PendingCountsDto.builder()
                .pendingSubscriptions(pendingSubscriptions)
                .pendingAdSubscriptions(pendingAdSubscriptions)
                .pendingAddSearches(pendingAddSearches)
                .build();
    }

    @Override
    public List<UserSubscriptionResponseDto> getHistoricalSubscriptions() {
        List<UserSubscription> all = userSubscriptionRepository.findAll();
        Map<String, List<UserSubscription>> byUser = all.stream()
                .filter(s -> s.getUserId() != null)
                .collect(Collectors.groupingBy(UserSubscription::getUserId));
        List<UserSubscriptionResponseDto> result = new ArrayList<>();
        for (List<UserSubscription> userSubs : byUser.values()) {
            if (userSubs.size() <= 1) continue;
            userSubs.sort((a, b) -> {
                LocalDateTime dateA = a.getSubmissionDate() != null ? a.getSubmissionDate() : LocalDateTime.MIN;
                LocalDateTime dateB = b.getSubmissionDate() != null ? b.getSubmissionDate() : LocalDateTime.MIN;
                return dateB.compareTo(dateA);
            });
            for (int i = 1; i < userSubs.size(); i++) {
                result.add(userSubscriptionMapper.toResponseDto(userSubs.get(i)));
            }
        }
        return result;
    }
}
