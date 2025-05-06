package com.rawneeded.mapper;

import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.model.Product;
import com.rawneeded.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-06T12:03:50+0000",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.5 (Amazon.com Inc.)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(ProductRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Product product = new Product();

        product.setSupplier( productRequestDTOToUser( dto ) );
        product.setName( dto.getName() );
        product.setOrigin( dto.getOrigin() );
        product.setContactPersonName( dto.getContactPersonName() );
        product.setContactPersonPhoneNumber( dto.getContactPersonPhoneNumber() );
        product.setInStock( dto.isInStock() );
        product.setCategory( dto.getCategory() );
        product.setSubCategory( dto.getSubCategory() );

        return product;
    }

    @Override
    public void update(Product user, ProductRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return;
        }

        if ( requestDTO.getName() != null ) {
            user.setName( requestDTO.getName() );
        }
        if ( requestDTO.getOrigin() != null ) {
            user.setOrigin( requestDTO.getOrigin() );
        }
        if ( requestDTO.getContactPersonName() != null ) {
            user.setContactPersonName( requestDTO.getContactPersonName() );
        }
        if ( requestDTO.getContactPersonPhoneNumber() != null ) {
            user.setContactPersonPhoneNumber( requestDTO.getContactPersonPhoneNumber() );
        }
        user.setInStock( requestDTO.isInStock() );
        if ( requestDTO.getCategory() != null ) {
            user.setCategory( requestDTO.getCategory() );
        }
        if ( requestDTO.getSubCategory() != null ) {
            user.setSubCategory( requestDTO.getSubCategory() );
        }
    }

    @Override
    public ProductResponseDTO toResponseDto(Product user) {
        if ( user == null ) {
            return null;
        }

        ProductResponseDTO productResponseDTO = new ProductResponseDTO();

        productResponseDTO.setSupplierId( userSupplierId( user ) );
        productResponseDTO.setSupplierName( userSupplierName( user ) );
        productResponseDTO.setId( user.getId() );
        productResponseDTO.setName( user.getName() );
        productResponseDTO.setOrigin( user.getOrigin() );
        productResponseDTO.setContactPersonName( user.getContactPersonName() );
        productResponseDTO.setContactPersonPhoneNumber( user.getContactPersonPhoneNumber() );
        productResponseDTO.setInStock( user.isInStock() );
        productResponseDTO.setCategory( user.getCategory() );
        productResponseDTO.setSubCategory( user.getSubCategory() );

        return productResponseDTO;
    }

    @Override
    public CartItemDTO toCartItemDto(Product product) {
        if ( product == null ) {
            return null;
        }

        CartItemDTO cartItemDTO = new CartItemDTO();

        cartItemDTO.setSupplierId( userSupplierId( product ) );
        cartItemDTO.setSupplierName( userSupplierName( product ) );
        cartItemDTO.setId( product.getId() );
        cartItemDTO.setName( product.getName() );
        cartItemDTO.setOrigin( product.getOrigin() );
        cartItemDTO.setContactPersonName( product.getContactPersonName() );
        cartItemDTO.setContactPersonPhoneNumber( product.getContactPersonPhoneNumber() );
        cartItemDTO.setInStock( product.isInStock() );

        return cartItemDTO;
    }

    protected User productRequestDTOToUser(ProductRequestDTO productRequestDTO) {
        if ( productRequestDTO == null ) {
            return null;
        }

        User user = new User();

        user.setId( productRequestDTO.getSupplierId() );

        return user;
    }

    private String userSupplierId(Product product) {
        if ( product == null ) {
            return null;
        }
        User supplier = product.getSupplier();
        if ( supplier == null ) {
            return null;
        }
        String id = supplier.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String userSupplierName(Product product) {
        if ( product == null ) {
            return null;
        }
        User supplier = product.getSupplier();
        if ( supplier == null ) {
            return null;
        }
        String name = supplier.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
