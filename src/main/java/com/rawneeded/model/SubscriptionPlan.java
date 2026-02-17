package com.rawneeded.model;

import com.rawneeded.enumeration.BillingFrequency;
import com.rawneeded.enumeration.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubscriptionPlan {
    private String id;
    private String name;
    private double pricePerUser; // Base subscription price per user
    private String description;
    private BillingFrequency billingFrequency;
    private List<SpecialOffer> specialOffers;
    private PlanType planType;
    
    // Features list with prices
    private List<PlanFeature> features;
    
    // For Customer plans: Product searches configuration
    private ProductSearchesConfig productSearchesConfig;

    
    private boolean free = false;
    private boolean exclusive = false;
    private boolean active = true;
}

