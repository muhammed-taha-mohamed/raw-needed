package com.rawneeded.mapper;

import com.rawneeded.dto.category.CategoryRequestDto;
import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryRequestDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import com.rawneeded.model.Category;
import com.rawneeded.model.SubCategory;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequestDto dto);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget Category category, CategoryRequestDto dto);

    CategoryResponseDto toResponseDto(Category category);
}