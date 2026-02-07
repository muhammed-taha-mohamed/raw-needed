package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddSearchesRequestDto {
    private String id;
    private String userId;
    private String subscriptionId;
    private String planName;
    private String userOrganizationName;
    private int numberOfSearches;
    private double totalPrice;
    private String receiptFilePath;
    private UserSubscriptionStatus status;
    private LocalDateTime createdAt;
}
