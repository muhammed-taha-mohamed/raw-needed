package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingCountsDto {
    private long pendingSubscriptions;
    private long pendingAdSubscriptions;
    private long pendingAddSearches;
}
