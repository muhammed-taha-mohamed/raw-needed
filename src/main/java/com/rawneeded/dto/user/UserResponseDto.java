package com.rawneeded.dto.user;


import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserResponseDto {
    private String id;
    private String name;
    private Role role;
    private String email;
    private String phoneNumber;
    private CategoryResponseDto category;
    private List<SubCategoryResponseDto> subCategories;
    private UserSubscriptionResponseDto subscription;
    private List<String> allowedScreens;
    private String profileImage;
}