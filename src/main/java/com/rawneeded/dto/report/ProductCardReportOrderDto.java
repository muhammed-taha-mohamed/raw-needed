package com.rawneeded.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardReportOrderDto {
    private String lineId;
    private String orderId;
    private String orderNumber;
    private LocalDateTime orderCreatedAt;

    private String customerOwnerId;
    private String customerName;
    private String customerOrganizationName;

    private String status;
    private Float quantity;
    private String unit;
    private Boolean manualOrder;
}
