package com.rawneeded.dto.advertisement;

import jakarta.validation.constraints.Min;
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
public class UpdateAdPackageRequestDto {
    private String nameAr;
    private String nameEn;
    @Min(1)
    private Integer numberOfDays;
    @Min(0)
    private BigDecimal pricePerAd;
    @Min(0)
    private BigDecimal featuredPrice;
    /** Special offers based on number of ads (discounts) */
    private List<AdSpecialOffer> specialOffers;
    private Boolean active;
    private Integer sortOrder;
}
