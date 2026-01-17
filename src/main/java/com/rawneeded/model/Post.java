package com.rawneeded.model;

import com.rawneeded.enumeration.PostStatus;
import com.rawneeded.enumeration.PostType;
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
public class Post {
    private String id;
    private String materialName;
    private String image;
    private Float quantity;
    private String unit;
    private PostType postType;
    
    @DBRef
    private User createdBy;
    private String createdById;
    private String createdByName;
    private String createdByOrganizationName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private boolean active = true;
    private PostStatus status = PostStatus.OPEN;
}
