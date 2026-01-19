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
    private double pricePerUser;
    private String description;
    private BillingFrequency billingFrequency;
    private List<SpecialOffer> specialOffers;
    private PlanType planType;
    private List<String> features;
    private boolean exclusive = false;
    private boolean active = true;
    private boolean hasAdvertisements = false;
}

