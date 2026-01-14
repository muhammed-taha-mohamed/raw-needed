package com.rawneeded.model;

import com.rawneeded.enumeration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String id;
    
    @Indexed
    private String userId; // The user who will receive this notification
    
    private NotificationType type;
    
    private String titleEn;
    private String titleAr;
    
    private String messageEn;
    private String messageAr;
    
    private boolean read;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime readAt;
    
    // Reference to related entity (e.g., orderId, quotationId)
    private String relatedEntityId;
    
    private String relatedEntityType; // e.g., "ORDER", "QUOTATION", "RFQ"
    
    // Additional metadata as JSON string or Map
    private String metadata;
}
