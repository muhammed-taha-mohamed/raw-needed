package com.rawneeded.dto.RFQ;

import com.rawneeded.dto.product.CartItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRFQRequest {
    private String userId;
    private List<CartItemDTO> items;
    private String specialOfferId; // Optional: ID of special offer if order is from special offer
}