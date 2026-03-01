package com.rawneeded.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardChartsDto {
    private List<TimeSeriesPointDto> ordersOverTime;
    private List<TimeSeriesPointDto> subscriptionsOverTime;
    private List<TimeSeriesPointDto> revenueOverTime;
    private List<PieSliceDto> ordersByStatus;
    private List<PieSliceDto> usersByRole;
}
