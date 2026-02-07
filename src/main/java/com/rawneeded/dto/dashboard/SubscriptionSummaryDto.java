package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionSummaryDto {
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long pendingSubscriptions;
    private long subscriptionsThisMonth;
}
