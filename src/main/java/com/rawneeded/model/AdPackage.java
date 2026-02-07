package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ad package: admin sets price per ad and display days per ad.
 * Supplier chooses number of ads; cost = numberOfAds * pricePerAd.
 */
@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdPackage {
    private String id;
    private String nameAr;
    private String nameEn;
    /** Display duration per ad (days) */
    private int numberOfDays;
    /** Price per ad (EGP) - set by admin */
    private BigDecimal pricePerAd;
    /** Extra price for featuring ad at top (EGP) - set by admin */
    private BigDecimal featuredPrice;
    /** @deprecated Use pricePerAd - kept for backward compatibility */
    @Deprecated
    private BigDecimal price;
    @Builder.Default
    private boolean active = true;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
