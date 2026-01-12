package com.rawneeded.mapper;

import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import com.rawneeded.dto.subscription.UserSubscriptionInfo;
import com.rawneeded.dto.subscription.UserSubscriptionResponseDto;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.SupplierResponseDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.model.Category;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSubscription;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring" )
public interface UserMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    User toEntity(CreateUserDto dto);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "category", ignore = true)
    void update(@MappingTarget User user, UserRequestDto requestDTO);

    @Mapping(source = "category", target = "category", qualifiedByName = "categoryToDto")
    @Mapping(source = "subscription", target = "subscription", qualifiedByName = "subscriptionToDto")
    UserResponseDto toResponseDto(User user);

    @Mapping(source = "category", target = "category", qualifiedByName = "categoryToDto")
    SupplierResponseDto toSupplierResponseDto(User user);

    @Named("categoryToDto")
    default CategoryResponseDto categoryToDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .arabicName(category.getArabicName())
                .build();
    }


    @Named("subscriptionToDto")
    default UserSubscriptionInfo subscriptionToDto(UserSubscription userSubscription) {
        if (userSubscription == null) {
            return null;
        }
        return UserSubscriptionInfo.builder()
                .id(userSubscription.getId())
                .numberOfUsers(userSubscription.getNumberOfUsers())
                .remainingUsers(userSubscription.getRemainingUsers())
                .usedUsers(userSubscription.getUsedUsers())
                .planId(userSubscription.getPlan().getId())
                .planName(userSubscription.getPlan().getName())
                .subscriptionDate(userSubscription.getSubscriptionDate())
                .expiryDate(userSubscription.getExpiryDate())
                .build();
    }
    default Page<UserResponseDto> toResponsePages(Page<User> users){
        return users.map(this::toResponseDto);
    }

    default Page<SupplierResponseDto> toSupplierResponsePages(Page<User> users){
        return users.map(this::toSupplierResponseDto);
    }
}
