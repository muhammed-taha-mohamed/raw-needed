package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Records when an advertisement was viewed by a user
 */
@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementView {
    private String id;

    @DBRef
    private Advertisement advertisement;
    private String advertisementId;

    @DBRef
    private User viewer;
    private String viewerId;

    private LocalDateTime viewedAt;
}
