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
public class Advertisement {
    private String id;

    @DBRef
    private User user;
    private String userId;

    private String image;
    private String text;

    /** Ad display start date */
    private LocalDateTime startDate;
    /** Ad display end date - ad is hidden after */
    private LocalDateTime endDate;
    /** Feature ad at top (supplier pays extra) */
    @Builder.Default
    private boolean featured = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean active = true;

    /** Ad is hidden (not shown to customers) but not deleted */
    @Builder.Default
    private boolean hidden = false;
}
