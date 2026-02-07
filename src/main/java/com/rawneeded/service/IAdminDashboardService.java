package com.rawneeded.service;

import com.rawneeded.dto.dashboard.AdSubscriptionStatsDto;
import com.rawneeded.dto.dashboard.DashboardStatsDto;
import com.rawneeded.dto.dashboard.PendingCountsDto;
import com.rawneeded.dto.dashboard.SubscriptionSummaryDto;
import com.rawneeded.dto.dashboard.UserStatsDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;

import java.util.List;

public interface IAdminDashboardService {
    DashboardStatsDto getDashboardStats();

    SubscriptionSummaryDto getSubscriptionSummary();

    UserStatsDto getUserStats();

    AdSubscriptionStatsDto getAdSubscriptionStats();

    PendingCountsDto getPendingCounts();

    List<UserSubscriptionResponseDto> getHistoricalSubscriptions();
}
