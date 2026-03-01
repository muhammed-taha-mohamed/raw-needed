package com.rawneeded.dto.dashboard;

import com.rawneeded.enumeration.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrderSummaryDto {
    private String id;
    private String orderNumber;
    private OrderStatus status;
    private String userName;
    private String organizationName;
    private int numberOfLines;
    private LocalDateTime createdAt;
}
