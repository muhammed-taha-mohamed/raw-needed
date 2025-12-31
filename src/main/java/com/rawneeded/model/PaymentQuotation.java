package com.rawneeded.model;

import com.rawneeded.enumeration.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentQuotation {
    private String id;
    
    @DBRef
    private User owner;
    private String ownerId;
    
    @DBRef
    private SubscriptionPlan plan;
    private String planId;
    
    private String filePath;

    private QuotationStatus status = QuotationStatus.PENDING;
    private LocalDateTime submissionDate = LocalDateTime.now();
}
