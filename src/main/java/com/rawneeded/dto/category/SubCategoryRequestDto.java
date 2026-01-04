package com.rawneeded.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubCategoryRequestDto {
    private String name;
    private String arabicName;
    private String categoryId;
}
