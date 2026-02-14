package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
@Builder
public class Category {

    private String id;

    private String name;

    private String arabicName;

    private boolean active = true;

    private List<CategoryExtraField> extraFields;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}