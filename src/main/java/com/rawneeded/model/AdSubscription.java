package com.rawneeded.model;

import com.rawneeded.enumeration.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Supplier subscription to an ad package: request package, upload payment proof, admin approves; then supplier can add ads.
 */
@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdSubscription {
    private String id;

    @DBRef
    private User supplier;
    private String supplierId;

    @DBRef
    private AdPackage adPackage;
    private String adPackageId;

    private UserSubscriptionStatus status;

    /** Payment proof file path (optional) */
    private String paymentProofPath;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    /** Subscription validity start (after approval) */
    private LocalDateTime startDate;
    /** Subscription validity end - after this supplier cannot add more ads */
    private LocalDateTime endDate;

    /** Snapshot of package at request time */
    private String packageNameAr;
    private String packageNameEn;
    private int numberOfDays;
    private BigDecimal pricePerAd;
    /** @deprecated Use pricePerAd - kept for backward compatibility */
    @Deprecated
    private BigDecimal price;
    /** Number of ads requested */
    private int numberOfAds;
    /** Total amount (numberOfAds * pricePerAd) */
    private BigDecimal totalPrice;
    /** Remaining ads for supplier after approval - decremented on each ad created */
    private int remainingAds;
}
