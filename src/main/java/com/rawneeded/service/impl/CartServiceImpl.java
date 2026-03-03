package com.rawneeded.service.impl;

import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.dto.product.AddItemsRequestDto;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.mapper.CartMapper;
import com.rawneeded.mapper.ProductMapper;
import com.rawneeded.model.Cart;
import com.rawneeded.model.Product;
import com.rawneeded.repository.CartRepository;
import com.rawneeded.repository.ProductRepository;
import com.rawneeded.service.ICartService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final MessagesUtil messagesUtil;


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
    public CartDTO addItemToCart(String userId, String productId, float quantity, String specialOfferId) {
        try {
            log.info("Start adding item to cart : {}", userId);

            Cart cart = cartRepository.findByUserId(userId)
                    .orElse(Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .build());

            cart.setItems(cart.getItems() == null ? new ArrayList<>() : cart.getItems());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("PRODUCT_NOT_FOUND")));

            // 🔍 check if item already exists
            Optional<CartItemDTO> existingItemOpt = cart.getItems().stream()
                    .filter(item -> item.getId().equals(productId))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                // Item already exists . Update quantity
                CartItemDTO existingItem = existingItemOpt.get();
                existingItem.setQuantity(quantity);
                if (specialOfferId != null) {
                    existingItem.setSpecialOfferId(specialOfferId);
                }
            } else {
                // Item does not exist . Add new
                CartItemDTO newItem = productMapper.toCartItemDto(product);
                newItem.setQuantity(quantity);
                if (specialOfferId != null) {
                    newItem.setSpecialOfferId(specialOfferId);
                }
                cart.getItems().add(newItem);
            }

            cart = cartRepository.save(cart);
            return cartMapper.toDTO(cart);

        } catch (Exception e) {
            log.error("Failed to add item to cart due to : {}", e.getMessage(), e);
            throw new AbstractException(e.getMessage());
        }
    }


    @Override
    public CartDTO removeItemFromCart(String userId, String productId) {
        try {
            log.info("Start removing item from cart for user: {}", userId);

            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("CART_NOT_FOUND")));

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new AbstractException(messagesUtil.getMessage("CART_IS_EMPTY"));
            }

            boolean removed = cart.getItems()
                    .removeIf(item -> item.getId().equals(productId));

            if (!removed) {
                throw new AbstractException(messagesUtil.getMessage("PRODUCT_NOT_IN_CART"));
            }

            cart = cartRepository.save(cart);
            return cartMapper.toDTO(cart);

        } catch (Exception e) {
            log.error("Failed to remove item from cart due to : {}", e.getMessage(), e);
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public CartDTO addItemsToCart(AddItemsRequestDto request) {

        String userId = request.getUserId();
        log.info("Start bulk adding items to cart for user: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // 1️⃣ Collect all productIds first
        Set<String> productIds = request.getItems().stream()
                .map(AddItemsRequestDto.AddItem::getProductId)
                .collect(Collectors.toSet());

        // 2️⃣ Fetch all products in ONE query
        Map<String, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3️⃣ Convert existing cart items to Map for O(1) lookup
        Map<String, CartItemDTO> cartItemMap = cart.getItems()
                .stream()
                .collect(Collectors.toMap(CartItemDTO::getId, item -> item));

        for (AddItemsRequestDto.AddItem addItem : request.getItems()) {

            String productId = addItem.getProductId();
            float quantity = addItem.getQuantity();
            String specialOfferId = addItem.getSpecialOfferId();

            Product product = productMap.get(productId);
            if (product == null) {
                throw new AbstractException(
                        messagesUtil.getMessage("PRODUCT_NOT_FOUND"));
            }

            CartItemDTO existingItem = cartItemMap.get(productId);

            if (existingItem != null) {
                existingItem.setQuantity(quantity);
                if (specialOfferId != null) {
                    existingItem.setSpecialOfferId(specialOfferId);
                }
            } else {
                CartItemDTO newItem = productMapper.toCartItemDto(product);
                newItem.setQuantity(quantity);
                if (specialOfferId != null) {
                    newItem.setSpecialOfferId(specialOfferId);
                }

                cart.getItems().add(newItem);
                cartItemMap.put(productId, newItem);
            }
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDTO(savedCart);
    }

}



