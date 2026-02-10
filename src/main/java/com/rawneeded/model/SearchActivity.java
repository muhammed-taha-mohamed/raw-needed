package com.rawneeded.model;

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
@NoArgsConstructor
@AllArgsConstructor
public class SearchActivity {
    private String id;

    @Indexed
    private String ownerId;

    @Indexed
    private String userId;

    private String userName;

    @Indexed
    private LocalDateTime searchedAt;

    private Boolean hasFilters;
}
