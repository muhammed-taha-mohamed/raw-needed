package com.rawneeded.service.impl;

import com.rawneeded.dto.advertisement.AdvertisementResponseDto;
import com.rawneeded.dto.dashboard.AdminDashboardOverviewDto;
import com.rawneeded.dto.dashboard.AdSubscriptionStatsDto;
import com.rawneeded.dto.dashboard.DashboardChartsDto;
import com.rawneeded.dto.dashboard.DashboardCountsDto;
import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.dto.dashboard.MonthlyOrderStats;
import com.rawneeded.dto.dashboard.PendingCountsDto;
import com.rawneeded.dto.dashboard.PieSliceDto;
import com.rawneeded.dto.dashboard.RecentComplaintSummaryDto;
import com.rawneeded.dto.dashboard.RecentOrderSummaryDto;
import com.rawneeded.dto.dashboard.SubscriptionSummaryDto;
import com.rawneeded.dto.dashboard.TimeSeriesPointDto;
import com.rawneeded.dto.dashboard.UserStatsDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.enumeration.ComplaintStatus;
import com.rawneeded.model.Advertisement;
import com.rawneeded.repository.AdvertisementRepository;
import com.rawneeded.repository.AdvertisementViewRepository;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.UserSubscriptionMapper;
import com.rawneeded.model.Complaint;
import com.rawneeded.model.RFQOrder;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.*;
import com.rawneeded.service.IAdminDashboardService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
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
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementViewRepository advertisementViewRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;
    private final ComplaintRepository complaintRepository;
    private final PostRepository postRepository;
    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public AdminDashboardOverviewDto getOverview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        SubscriptionSummaryDto subscriptionSummary = getSubscriptionSummary();
        UserStatsDto userStats = getUserStats();
        AdSubscriptionStatsDto adSubscriptionStats = getAdSubscriptionStats();
        PendingCountsDto pendingCounts = getPendingCounts();
        DashboardStatsDto dashboardStats = getDashboardStats();

        List<TimeSeriesPointDto> ordersOverTime = buildOrdersOverTime(12);
        List<TimeSeriesPointDto> subscriptionsOverTime = buildSubscriptionsOverTime(12);
        List<PieSliceDto> ordersByStatus = buildOrdersByStatus();
        List<PieSliceDto> usersByRole = buildUsersByRole();
        List<TimeSeriesPointDto> revenueOverTime = buildRevenueOverTime(12);

        List<RecentOrderSummaryDto> recentOrders = orderRepository.findFirst15ByOrderByCreatedAtDesc().stream()
                .map(o -> RecentOrderSummaryDto.builder()
                        .id(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .status(o.getStatus())
                        .userName(o.getUserName())
                        .organizationName(o.getOrganizationName())
                        .numberOfLines(o.getNumberOfLines())
                        .createdAt(o.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        List<RecentComplaintSummaryDto> recentComplaints = complaintRepository.findFirst10ByOrderByCreatedAtDesc().stream()
                .map(c -> RecentComplaintSummaryDto.builder()
                        .id(c.getId())
                        .subject(c.getSubject())
                        .status(c.getStatus())
                        .userId(c.getUserId())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalSubCategories = subCategoryRepository.count();
        long totalComplaints = complaintRepository.count();
        long openComplaints = complaintRepository.countByStatus(ComplaintStatus.OPEN);
        long totalPosts = postRepository.count();
        long totalOffers = offerRepository.count();
        long totalNotifications = notificationRepository.count();

        List<UserSubscription> approvedSubs = userSubscriptionRepository.findByStatus(UserSubscriptionStatus.APPROVED);
        double totalSubscriptionRevenue = approvedSubs.stream().mapToDouble(UserSubscription::getFinalPrice).sum();
        double subscriptionRevenueThisMonth = approvedSubs.stream()
                .filter(s -> s.getSubmissionDate() != null && !s.getSubmissionDate().isBefore(startOfThisMonth))
                .mapToDouble(UserSubscription::getFinalPrice)
                .sum();
        long addSearchesPending = addSearchesRequestRepository.countByStatus(UserSubscriptionStatus.PENDING);

        return AdminDashboardOverviewDto.builder()
                .subscriptionSummary(subscriptionSummary)
                .userStats(userStats)
                .adSubscriptionStats(adSubscriptionStats)
                .pendingCounts(pendingCounts)
                .dashboardStats(dashboardStats)
                .ordersOverTime(ordersOverTime)
                .subscriptionsOverTime(subscriptionsOverTime)
                .ordersByStatus(ordersByStatus)
                .usersByRole(usersByRole)
                .revenueOverTime(revenueOverTime)
                .recentOrders(recentOrders)
                .recentComplaints(recentComplaints)
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .totalSubCategories(totalSubCategories)
                .totalComplaints(totalComplaints)
                .openComplaints(openComplaints)
                .totalPosts(totalPosts)
                .totalOffers(totalOffers)
                .totalNotifications(totalNotifications)
                .totalSubscriptionRevenue(totalSubscriptionRevenue)
                .subscriptionRevenueThisMonth(subscriptionRevenueThisMonth)
                .addSearchesPending(addSearchesPending)
                .build();
    }

    private List<TimeSeriesPointDto> buildOrdersOverTime(int months) {
        List<TimeSeriesPointDto> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999);
            long count = orderRepository.countByCreatedAtBetween(start, end);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            result.add(TimeSeriesPointDto.builder().label(label).value(count).build());
        }
        return result;
    }

    private List<TimeSeriesPointDto> buildSubscriptionsOverTime(int months) {
        List<TimeSeriesPointDto> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999);
            long count = userSubscriptionRepository.countBySubmissionDateBetween(start, end);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            result.add(TimeSeriesPointDto.builder().label(label).value(count).build());
        }
        return result;
    }

    private List<PieSliceDto> buildOrdersByStatus() {
        List<RFQOrder> all = orderRepository.findAll();
        Map<OrderStatus, Long> byStatus = all.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(RFQOrder::getStatus, Collectors.counting()));
        List<PieSliceDto> result = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = byStatus.getOrDefault(status, 0L);
            result.add(PieSliceDto.builder().name(status.name()).value(count).build());
        }
        return result;
    }

    private List<PieSliceDto> buildUsersByRole() {
        List<PieSliceDto> result = new ArrayList<>();
        for (Role role : Role.values()) {
            long count = userRepository.countByRole(role);
            if (count > 0) {
                result.add(PieSliceDto.builder().name(role.name()).value(count).build());
            }
        }
        return result;
    }

    private List<TimeSeriesPointDto> buildRevenueOverTime(int months) {
        List<UserSubscription> approved = userSubscriptionRepository.findByStatus(UserSubscriptionStatus.APPROVED);
        Map<YearMonth, Double> revenueByMonth = approved.stream()
                .filter(s -> s.getSubmissionDate() != null)
                .collect(Collectors.groupingBy(
                        s -> YearMonth.from(s.getSubmissionDate()),
                        Collectors.summingDouble(UserSubscription::getFinalPrice)
                ));
        List<TimeSeriesPointDto> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            long value = revenueByMonth.getOrDefault(ym, 0.0).longValue();
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();
            result.add(TimeSeriesPointDto.builder().label(label).value(value).build());
        }
        return result;
    }

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

    @Override
    public List<AdvertisementResponseDto> getDashboardAdvertisements() {
        try {
            log.info("Fetching all active advertisements for dashboard (list, not paginated)");
            LocalDateTime now = LocalDateTime.now();
            
            // Get all advertisements first
            List<Advertisement> allAds = advertisementRepository.findAll();
            log.debug("Total advertisements in DB: {}", allAds.size());
            
            // Get all active, not expired, and not hidden advertisements
            List<Advertisement> advertisements = allAds.stream()
                    .filter(ad -> {
                        boolean isActive = ad.isActive();
                        boolean isNotHidden = !ad.isHidden();
                        boolean isNotExpired = ad.getEndDate() == null || ad.getEndDate().isAfter(now);
                        
                        if (!isActive) {
                            log.debug("Ad {} filtered out: not active", ad.getId());
                        }
                        if (!isNotHidden) {
                            log.debug("Ad {} filtered out: hidden", ad.getId());
                        }
                        if (!isNotExpired && ad.getEndDate() != null) {
                            log.debug("Ad {} filtered out: expired (endDate: {}, now: {})", ad.getId(), ad.getEndDate(), now);
                        }
                        
                        return isActive && isNotHidden && isNotExpired;
                    })
                    .sorted((a, b) -> {
                        // Sort by featured first, then by createdAt desc
                        if (a.isFeatured() != b.isFeatured()) {
                            return b.isFeatured() ? 1 : -1;
                        }
                        if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                            return b.getCreatedAt().compareTo(a.getCreatedAt());
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
            
            log.info("Found {} active advertisements for dashboard", advertisements.size());
            
            return advertisements.stream()
                    .map(ad -> {
                        Long remainingDays = null;
                        if (ad.getEndDate() != null) {
                            if (ad.getEndDate().isAfter(now)) {
                                long hoursRemaining = Duration.between(now, ad.getEndDate()).toHours();
                                remainingDays = hoursRemaining > 0 ? (hoursRemaining / 24) + (hoursRemaining % 24 > 0 ? 1 : 0) : 0L;
                            } else {
                                remainingDays = 0L;
                            }
                        }
                        
                        Long viewCount = advertisementViewRepository.countByAdvertisementId(ad.getId());
                        
                        return AdvertisementResponseDto.builder()
                                .id(ad.getId())
                                .userId(ad.getUserId())
                                .image(ad.getImage())
                                .text(ad.getText())
                                .startDate(ad.getStartDate())
                                .endDate(ad.getEndDate())
                                .featured(ad.isFeatured())
                                .createdAt(ad.getCreatedAt())
                                .updatedAt(ad.getUpdatedAt())
                                .active(ad.isActive())
                                .hidden(ad.isHidden())
                                .remainingDays(remainingDays)
                                .viewCount(viewCount)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching dashboard advertisements: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public DashboardChartsDto getDashboardCharts() {
        return DashboardChartsDto.builder()
                .ordersOverTime(buildOrdersOverTime(12))
                .subscriptionsOverTime(buildSubscriptionsOverTime(12))
                .revenueOverTime(buildRevenueOverTime(12))
                .ordersByStatus(buildOrdersByStatus())
                .usersByRole(buildUsersByRole())
                .build();
    }

    @Override
    public List<RecentOrderSummaryDto> getRecentOrders() {
        return orderRepository.findFirst15ByOrderByCreatedAtDesc().stream()
                .map(o -> RecentOrderSummaryDto.builder()
                        .id(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .status(o.getStatus())
                        .userName(o.getUserName())
                        .organizationName(o.getOrganizationName())
                        .numberOfLines(o.getNumberOfLines())
                        .createdAt(o.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentComplaintSummaryDto> getRecentComplaints() {
        return complaintRepository.findFirst10ByOrderByCreatedAtDesc().stream()
                .map(c -> RecentComplaintSummaryDto.builder()
                        .id(c.getId())
                        .subject(c.getSubject())
                        .status(c.getStatus())
                        .userId(c.getUserId())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public DashboardCountsDto getDashboardCounts() {
        LocalDateTime startOfThisMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalSubCategories = subCategoryRepository.count();
        long totalComplaints = complaintRepository.count();
        long openComplaints = complaintRepository.countByStatus(ComplaintStatus.OPEN);
        long totalPosts = postRepository.count();
        long totalOffers = offerRepository.count();
        long totalNotifications = notificationRepository.count();
        List<UserSubscription> approvedSubs = userSubscriptionRepository.findByStatus(UserSubscriptionStatus.APPROVED);
        double totalSubscriptionRevenue = approvedSubs.stream().mapToDouble(UserSubscription::getFinalPrice).sum();
        double subscriptionRevenueThisMonth = approvedSubs.stream()
                .filter(s -> s.getSubmissionDate() != null && !s.getSubmissionDate().isBefore(startOfThisMonth))
                .mapToDouble(UserSubscription::getFinalPrice)
                .sum();
        long addSearchesPending = addSearchesRequestRepository.countByStatus(UserSubscriptionStatus.PENDING);
        return DashboardCountsDto.builder()
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .totalSubCategories(totalSubCategories)
                .totalComplaints(totalComplaints)
                .openComplaints(openComplaints)
                .totalPosts(totalPosts)
                .totalOffers(totalOffers)
                .totalNotifications(totalNotifications)
                .totalSubscriptionRevenue(totalSubscriptionRevenue)
                .subscriptionRevenueThisMonth(subscriptionRevenueThisMonth)
                .addSearchesPending(addSearchesPending)
                .build();
    }
}
