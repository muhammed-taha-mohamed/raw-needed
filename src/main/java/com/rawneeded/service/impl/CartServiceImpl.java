package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.mapper.CartMapper;
import com.rawneeded.mapper.ProductMapper;
import com.rawneeded.model.Cart;
import com.rawneeded.model.Product;
import com.rawneeded.model.User;
import com.rawneeded.repository.CartRepository;
import com.rawneeded.repository.ProductRepository;
import com.rawneeded.service.ICartService;
import com.rawneeded.service.IUserService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rawneeded.enummeration.TemplateName.QUOTATION_TEMPLATE;
import static com.rawneeded.enummeration.TemplateName.WELCOME_TEMPLATE;


@Service
@Slf4j
@AllArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;


    @Override
    public void create(String userId) {
        try {
            log.info("Start creating a cart to user : {}", userId);
            cartRepository.save(
                    Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .build());
        } catch (Exception e) {
            log.error("Failed to create a cart due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


    @Override
    public CartDTO getUserCart(String userId) {
        try {
            log.info("Start getting user cart : {}", userId);
            Cart cart = cartRepository.findByUserId(userId).orElse(Cart.builder().userId(userId)
                    .items(new ArrayList<>())
                    .build());
            return cartMapper.toDTO(cart);
        } catch (Exception e) {
            log.error("Failed to get user cart due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public CartDTO clearCart(String userId) {
        try {
            log.info("Start clearing user cart : {}", userId);
            Cart cart = cartRepository.findByUserId(userId).orElse(Cart.builder().userId(userId)
                    .items(new ArrayList<>())
                    .build());
            cart.setItems(new ArrayList<>());
            cart = cartRepository.save(cart);
            return cartMapper.toDTO(cart);
        } catch (Exception e) {
            log.error("Failed to clear user cart due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public CartDTO addItemToCart(String userId, String productId, float quantity) {
        try {
            log.info("Start adding item to cart : {}", userId);
            Cart cart = cartRepository.findByUserId(userId)
                    .orElse(Cart.builder().userId(userId)
                            .items(new ArrayList<>())
                            .build());

            cart.setItems(cart.getItems() == null ? new ArrayList<>() : cart.getItems());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new AbstractException("Product not found"));


            CartItemDTO newItem = productMapper.toCartItemDto(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);

            cart = cartRepository.save(cart);
            return cartMapper.toDTO(cart);
        } catch (Exception e) {
            log.error("Failed to add item to cart due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


}



