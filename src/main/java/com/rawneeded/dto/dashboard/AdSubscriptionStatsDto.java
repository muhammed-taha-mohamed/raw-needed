package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdSubscriptionStatsDto {
    private long totalAdSubscriptions;
    private long activeAdSubscriptions;
    private long pendingAdSubscriptions;
}
