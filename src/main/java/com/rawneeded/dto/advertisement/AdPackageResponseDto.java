package com.rawneeded.dto.advertisement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.rawneeded.model.AdSpecialOffer;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdPackageResponseDto {
    private String id;
    private String nameAr;
    private String nameEn;
    private int numberOfDays;
    private BigDecimal pricePerAd;
    private BigDecimal featuredPrice;
    /** Special offers based on number of ads (discounts) */
    private List<AdSpecialOffer> specialOffers;
    private boolean active;
    private int sortOrder;
}
