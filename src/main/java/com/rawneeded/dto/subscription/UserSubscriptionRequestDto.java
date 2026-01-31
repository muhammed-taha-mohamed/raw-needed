package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.PlanFeatures;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserSubscriptionRequestDto {
    private String planId;
    private int numberOfUsers;
    private String subscriptionFile;
    
    // For Customer plans: Number of product searches requested
    private Integer numberOfSearches;
    
    // Selected features
    private List<PlanFeatures> selectedFeatures;
}
