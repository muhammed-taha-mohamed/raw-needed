package com.rawneeded.dto.subscription;

import com.rawneeded.enumeration.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserSubscriptionResponseDto {
    private String id;
    private String userId;
    private String planId;
    private String planName;
    private int numberOfUsers;
    private int usedUsers;
    private int remainingUsers;
    private double total;
    private double discount;
    private double finalPrice;
    private String filePath;
    private UserSubscriptionStatus status;
    private LocalDateTime submissionDate;
    private LocalDateTime subscriptionDate ;
    private LocalDateTime expiryDate;
}
