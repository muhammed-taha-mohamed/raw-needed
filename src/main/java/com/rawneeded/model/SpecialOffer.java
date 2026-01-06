package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SpecialOffer {
    private int minUserCount; // Minimum number of users to qualify for this offer
    private double discountPercentage; // Discount percentage (e.g., 10 for 10%)
    private String description; // Optional description of the offer
}
