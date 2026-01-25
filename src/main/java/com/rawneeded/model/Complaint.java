package com.rawneeded.model;

import com.rawneeded.enumeration.ComplaintStatus;
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
public class Complaint {
    private String id;
    
    @DBRef
    private User user;
    private String userId;
    
    private String subject;
    private String description;
    private String image;
    
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
}
