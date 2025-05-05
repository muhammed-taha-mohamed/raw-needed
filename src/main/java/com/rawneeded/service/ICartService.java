package com.rawneeded.service;

import com.rawneeded.dto.product.CartDTO;

public interface ICartService {


    void create(String userId);

    CartDTO getUserCart(String userId);

    CartDTO clearCart(String userId);

    CartDTO addItemToCart(String userId, String productId);

}
