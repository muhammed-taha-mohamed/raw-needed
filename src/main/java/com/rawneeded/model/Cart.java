package com.rawneeded.model;


import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
@Builder
public class Cart {
    private String id;
    private String userId;
    private List<CartItemDTO> items;
}
