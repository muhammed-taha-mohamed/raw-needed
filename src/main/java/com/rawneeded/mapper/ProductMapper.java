package com.rawneeded.mapper;

import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.model.Product;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source ="supplierId" , target = "supplier.id" )
    Product toEntity(ProductRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void update(@MappingTarget Product user, ProductRequestDTO requestDTO);

    @Mappings({
            @Mapping(source ="supplier.id" , target = "supplierId" ),
            @Mapping(source ="supplier.name" , target = "supplierName" )
    })
    ProductResponseDTO toResponseDto(Product user);

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
