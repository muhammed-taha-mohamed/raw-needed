package com.rawneeded.mapper;

import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.model.Cart;
import com.rawneeded.model.Product;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartDTO toDTO(Cart dto);


}
