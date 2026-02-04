package com.rawneeded.dto.specialoffer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpecialOfferResponseDto {
    private String id;
    private String supplierId;
    private String supplierName;
    private String supplierOrganizationName;
    private String productId;
    private String productName;
    private String productImage;
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
