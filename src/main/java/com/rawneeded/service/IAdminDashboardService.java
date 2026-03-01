package com.rawneeded.service;

import com.rawneeded.dto.advertisement.AdvertisementResponseDto;
import com.rawneeded.dto.dashboard.AdminDashboardOverviewDto;
import com.rawneeded.dto.dashboard.AdSubscriptionStatsDto;
import com.rawneeded.dto.dashboard.DashboardChartsDto;
import com.rawneeded.dto.dashboard.DashboardCountsDto;
import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.dto.dashboard.PendingCountsDto;
import com.rawneeded.dto.dashboard.RecentComplaintSummaryDto;
import com.rawneeded.dto.dashboard.RecentOrderSummaryDto;
import com.rawneeded.dto.dashboard.SubscriptionSummaryDto;
import com.rawneeded.dto.dashboard.UserStatsDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;

import java.util.List;

public interface IAdminDashboardService {

    AdminDashboardOverviewDto getOverview();

    DashboardStatsDto getDashboardStats();

    SubscriptionSummaryDto getSubscriptionSummary();

    UserStatsDto getUserStats();

    AdSubscriptionStatsDto getAdSubscriptionStats();

    PendingCountsDto getPendingCounts();

    List<UserSubscriptionResponseDto> getHistoricalSubscriptions();

    List<AdvertisementResponseDto> getDashboardAdvertisements();

    DashboardChartsDto getDashboardCharts();

    List<RecentOrderSummaryDto> getRecentOrders();

    List<RecentComplaintSummaryDto> getRecentComplaints();

    DashboardCountsDto getDashboardCounts();
}
