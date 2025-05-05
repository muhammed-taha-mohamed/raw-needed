package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.product.CartDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.mapper.ProductMapper;
import com.rawneeded.model.Cart;
import com.rawneeded.model.Product;
import com.rawneeded.repository.CartRepository;
import com.rawneeded.service.ICartService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rawneeded.enummeration.TemplateName.WELCOME_TEMPLATE;


@Service
@Slf4j
@AllArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final MessagesUtil messagesUtil;
    private final NotificationService notificationService;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;


    @Override
    public void create(String userId) {
        try {
            log.info("Start creating a cart to user : {}", userId);
            cartRepository.save(Cart.builder().userId(userId).build());
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
            return mapToDTO(cart);
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
            return mapToDTO(cart);
        } catch (Exception e) {
            log.error("Failed to clear user cart due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public CartDTO addItemToCart(String userId, String productId) {
        try {
            log.info("Start adding item to cart : {}", userId);
            Cart cart = cartRepository.findByUserId(userId)
                    .orElse(Cart.builder().userId(userId)
                            .items(new ArrayList<>())
                            .build());
            cart.getItems().add(Product.builder()
                    .id(productId)
                    .build());

            cart = cartRepository.save(cart);
            return mapToDTO(cart);
        } catch (Exception e) {
            log.error("Failed to add item to cart due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    public void sendQuotationRequests(String userId) {
        try {

            Cart cart = cartRepository.findByUserId(userId).orElse(Cart.builder().userId(userId)
                    .items(new ArrayList<>())
                    .build());
            List<Product> products = cart.getItems();


            String subject = messagesUtil.getMessage("new.quotation.subject");


            notificationService.sendEmail(
                    MailDto.builder()
                            .toEmail("mohamedtahaomk35@gmail.com")
                            .subject("Test")
                            .templateName(WELCOME_TEMPLATE)
                            .model(Map.of(
                                    "supplier_name", "testMail",
                                    "customer_name", "",
                                    "customer_email", "",
                                    "customer_phone", "",
                                    "data", ""
                                    ))
                            .build()
            );

            log.info("Start sending quotation requests to suppliers : {}", userId);
        } catch (Exception e) {
            log.error("Failed to send quotation requests to suppliers due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }


    private CartDTO mapToDTO(Cart cart) {
        return CartDTO.builder()
                .userId(cart.getUserId())
                .items(productMapper.toResponseList(cart.getItems()))
                .build();
    }

}



