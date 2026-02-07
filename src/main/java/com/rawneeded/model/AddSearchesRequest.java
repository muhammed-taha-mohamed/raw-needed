package com.rawneeded.model;

import com.rawneeded.enumeration.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Customer request to add more product searches to existing subscription (partial renewal).
 * Admin approves after verifying payment; then searches are added to UserSubscription.
 */
@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddSearchesRequest {
    private String id;

    private String userId;

    @DBRef
    private UserSubscription subscription;
    private String subscriptionId;

    private int numberOfSearches;
    private double totalPrice;

    /** Payment receipt file path (e.g. uploaded image URL) */
    private String receiptFilePath;

    private UserSubscriptionStatus status;

    private LocalDateTime createdAt;
}
