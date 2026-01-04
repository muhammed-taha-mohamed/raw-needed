package com.rawneeded.mapper;

import com.rawneeded.dto.category.SubCategoryRequestDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import com.rawneeded.model.SubCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SubCategoryMapper {

    SubCategory toEntity(SubCategoryRequestDto dto);

    SubCategoryResponseDto toResponseDto(SubCategory entity);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget SubCategory entity, SubCategoryRequestDto dto);
}