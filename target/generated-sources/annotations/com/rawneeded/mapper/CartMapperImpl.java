package com.rawneeded.mapper;

import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.model.Cart;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-06T12:03:50+0000",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.5 (Amazon.com Inc.)"
)
@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public CartDTO toDTO(Cart dto) {
        if ( dto == null ) {
            return null;
        }

        CartDTO cartDTO = new CartDTO();

        cartDTO.setUserId( dto.getUserId() );
        List<CartItemDTO> list = dto.getItems();
        if ( list != null ) {
            cartDTO.setItems( new ArrayList<CartItemDTO>( list ) );
        }

        return cartDTO;
    }
}
