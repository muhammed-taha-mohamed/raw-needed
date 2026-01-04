package com.rawneeded.mapper;

import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.model.Category;
import com.rawneeded.model.Product;
import com.rawneeded.model.SubCategory;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source ="supplierId" , target = "supplier.id" )
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    void update(@MappingTarget Product user, ProductRequestDTO requestDTO);

    @Mappings({
            @Mapping(source ="supplier.id" , target = "supplierId" ),
            @Mapping(source ="supplier.name" , target = "supplierName" ),
            @Mapping(source = "category", target = "category", qualifiedByName = "categoryToDto"),
            @Mapping(source = "subCategory", target = "subCategory", qualifiedByName = "subCategoryToDto")
    })
    ProductResponseDTO toResponseDto(Product user);

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

    @Named("subCategoryToDto")
    default SubCategoryResponseDto subCategoryToDto(SubCategory subCategory) {
        if (subCategory == null) {
            return null;
        }
        return SubCategoryResponseDto.builder()
                .name(subCategory.getName())
                .arabicName(subCategory.getArabicName())
                .build();
    }

    default Page<ProductResponseDTO> toResponsePages(Page<Product> products) {
        return products.map(this::toResponseDto);
    }

    default List<ProductResponseDTO> toResponseList(List<Product> products) {
        return products.stream().map(this::toResponseDto).toList();
    }


    @Mappings({
            @Mapping(source ="supplier.id" , target = "supplierId" ),
            @Mapping(source ="supplier.name" , target = "supplierName" )
    })
    CartItemDTO toCartItemDto(Product product);
}
