package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AdSpecialOffer {
    /** Minimum number of ads to qualify for this offer */
    private int minAdCount;
    /** Discount percentage (e.g., 10 for 10%) */
    private double discountPercentage;
    /** Optional description of the offer */
    private String description;
}
