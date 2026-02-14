package com.rawneeded.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.rawneeded.model.CategoryExtraField;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CategoryResponseDto {
    private String id;
    private String name;
    private String arabicName;
    private List<CategoryExtraField> extraFields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

