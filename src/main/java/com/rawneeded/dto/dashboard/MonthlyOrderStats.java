package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyOrderStats {
    private String month; // Format: "YYYY-MM" or "January 2024"
    private long orderCount;
    private double growthPercentage; // Growth compared to the previous month
}
