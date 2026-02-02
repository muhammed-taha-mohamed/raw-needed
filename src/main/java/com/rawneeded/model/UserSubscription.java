package com.rawneeded.model;

import com.rawneeded.enumeration.PlanFeatures;
import com.rawneeded.enumeration.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserSubscription {
    private String id;
    
    @DBRef
    private User user;
    private String userId;
    
    @DBRef
    private SubscriptionPlan plan;
    private String planId;
    
    private int numberOfUsers;
    private int usedUsers;
    private int remainingUsers;
    
    // For Customer plans: Product searches
    private Integer numberOfSearches; // Total searches purchased
    private Integer remainingSearches; // Remaining searches
    private Integer points; // Points earned from searches

    private List<PlanFeatures> selectedFeatures;

    private double total;
    private double discount;
    private double finalPrice;
    
    private String filePath;
    

    private UserSubscriptionStatus status;
    

    private LocalDateTime subscriptionDate ;
    private LocalDateTime expiryDate;
    private LocalDateTime submissionDate = LocalDateTime.now();

}
