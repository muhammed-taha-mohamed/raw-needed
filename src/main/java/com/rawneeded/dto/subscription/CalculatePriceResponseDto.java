package com.rawneeded.dto.subscription;

import com.rawneeded.model.SpecialOffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalculatePriceResponseDto {
    private String planId;
    private String planName;
    private double pricePerUser;
    private int numberOfUsers;
    private double total; // Total price before discount (pricePerUser * numberOfUsers)
    private double discount; // Discount amount if any offer is applied
    private double finalPrice; // Total price after discount
    private SpecialOffer appliedOffer; // The offer that was applied (if any)
    private List<SpecialOffer> availableOffers; // All available offers for this number of users
}
