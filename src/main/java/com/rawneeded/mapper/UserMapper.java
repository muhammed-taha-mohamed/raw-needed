package com.rawneeded.mapper;

import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.model.Category;
import com.rawneeded.model.User;
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
    UserResponseDto toResponseDto(User user);

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

    default Page<UserResponseDto> toResponsePages(Page<User> users){
        return users.map(this::toResponseDto);
    }

}
