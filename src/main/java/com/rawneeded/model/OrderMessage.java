package com.rawneeded.model;

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
public class OrderMessage {
    private String id;
    
    // Order reference
    private String orderId;
    
    @DBRef
    private User user;
    private String userId;
    private String userName;
    private String userOrganizationName;
    
    private String message;
    private String image;
    
    private LocalDateTime createdAt;
}
