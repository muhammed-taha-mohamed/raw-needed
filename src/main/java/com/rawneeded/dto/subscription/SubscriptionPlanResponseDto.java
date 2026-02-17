package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.BillingFrequency;
import com.rawneeded.enumeration.PlanType;
import com.rawneeded.model.PlanFeature;
import com.rawneeded.model.ProductSearchesConfig;
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
    private PlanType planType;
    
    // Features list with prices
    private List<PlanFeature> features;
    
    // For Customer plans: Product searches configuration
    private ProductSearchesConfig productSearchesConfig;

    
    private boolean free;
    private boolean exclusive;
    private boolean active;
}

