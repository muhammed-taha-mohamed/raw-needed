package com.rawneeded.service;

import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.dto.product.AddItemsRequestDto;

public interface ICartService {


    void create(String userId);

    CartDTO getUserCart(String userId);

    CartDTO clearCart(String userId);

    CartDTO addItemToCart(String userId, String productId, float quantity, String specialOfferId);

    CartDTO removeItemFromCart(String userId, String productId);

    CartDTO addItemsToCart(AddItemsRequestDto request);
}
