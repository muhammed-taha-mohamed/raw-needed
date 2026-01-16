package com.rawneeded.dto.RFQ;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierResponseOnOrderDTO {
    private float price;
    private float shippingCost;
    private LocalDate estimatedDelivery;
    private LocalDateTime respondedAt;
    private float availableQuantity;
    private String shippingInfo;
}
