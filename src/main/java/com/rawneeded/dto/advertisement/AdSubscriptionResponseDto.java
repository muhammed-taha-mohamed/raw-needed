package com.rawneeded.dto.advertisement;

import com.rawneeded.enumeration.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdSubscriptionResponseDto {
    private String id;
    private String supplierId;
    private String supplierName;
    private String supplierOrganizationName;
    private String supplierImage;
    private String adPackageId;
    private UserSubscriptionStatus status;
    private String paymentProofPath;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String packageNameAr;
    private String packageNameEn;
    private int numberOfDays;
    private BigDecimal pricePerAd;
    private int numberOfAds;
    private boolean featured;
    private BigDecimal totalPrice;
    private BigDecimal discount;
    private BigDecimal finalPrice;
    private int remainingAds;
}
