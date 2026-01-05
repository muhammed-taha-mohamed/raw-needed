package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.BillingFrequency;
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
public class SubscriptionPlanResponseDto {
    private String id;
    private String name;
    private double pricePerUser;
    private String description;
    private BillingFrequency billingFrequency;
    private List<SpecialOffer> specialOffers;
}

