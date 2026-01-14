package com.rawneeded.dto.user;


import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SupplierInfo {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private CategoryResponseDto category;
    private List<SubCategoryResponseDto> subCategories;
    private String profileImage;
    private String organizationName;
    private String organizationCRN;
}