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
public class ComplaintMessage {
    private String id;
    
    @DBRef
    private Complaint complaint;
    private String complaintId;
    
    @DBRef
    private User user;
    private String userId;
    
    private String message;
    private String image;
    
    @Builder.Default
    private boolean isAdmin = false;
    
    private LocalDateTime createdAt;
}
