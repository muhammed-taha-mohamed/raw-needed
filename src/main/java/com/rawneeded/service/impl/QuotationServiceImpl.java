package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.model.Cart;
import com.rawneeded.model.Product;
import com.rawneeded.model.User;
import com.rawneeded.repository.CartRepository;
import com.rawneeded.service.IQuotationService;
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

@Service
@AllArgsConstructor
@Slf4j
public class QuotationServiceImpl implements IQuotationService {
    private final CartRepository cartRepository;
    private final MessagesUtil messagesUtil;
    private final NotificationService notificationService;
    private final MongoTemplate mongoTemplate;
    private final IUserService userService;

    @Override
    public void sendQuotationRequests(String userId) {
        try {

            User user = mongoTemplate.findById(userId, User.class);
            Cart cart = cartRepository.findByUserId(userId).orElse(Cart.builder().userId(userId)
                    .items(new ArrayList<>())
                    .build());
            List<CartItemDTO> products = cart.getItems();

            Map<String, List<CartItemDTO>> productsGroupedBySupplier = products.stream()
                    .filter(product -> product.getSupplierId() != null)
                    .collect(Collectors.groupingBy(CartItemDTO::getSupplierId));

            String subject = messagesUtil.getMessage("new.quotation.subject");

            for (Map.Entry<String, List<CartItemDTO>> entry : productsGroupedBySupplier.entrySet()) {
                UserResponseDto supplier = userService.findById(entry.getKey());
                List<CartItemDTO> supplierItems = entry.getValue();

                String supplierName = supplier.getName();
                String supplierEmail = supplier.getEmail();

                StringBuilder dataBuilderEN = new StringBuilder();
                dataBuilderEN.append("<table border='1' cellspacing='0' cellpadding='5' style='width: 100%;'>")
                        .append("<thead><tr>")
                        .append("<th>Product</th>")
                        .append("<th>Origin</th>")
                        .append("<th>Quantity</th>")
                        .append("</tr></thead><tbody>");

                for (CartItemDTO item : supplierItems) {
                    dataBuilderEN.append("<tr>")
                            .append("<td>").append(item.getName()).append("</td>")
                            .append("<td>").append(item.getOrigin()).append("</td>")
                            .append("<td>").append(item.getQuantity()).append("</td>")
                            .append("</tr>");
                }

                dataBuilderEN.append("</tbody></table>");


                StringBuilder dataBuilderAR = new StringBuilder();
                dataBuilderAR.append("<table border='1' cellspacing='0' cellpadding='5'  style='width: 100%;'>")
                        .append("<thead><tr>")
                        .append("<th style='text-align: right;'>الكمية</th>")
                        .append("<th style='text-align: right;'>المنشأ</th>")
                        .append("<th style='text-align: right;'>المنتج</th>")
                        .append("</tr></thead><tbody>");

                for (CartItemDTO item : supplierItems) {
                    dataBuilderAR.append("<tr>")
                            .append("<td style='text-align: right;'>").append(item.getQuantity()).append("</td>")
                            .append("<td style='text-align: right;'>").append(item.getOrigin()).append("</td>")
                            .append("<td style='text-align: right;'>").append(item.getName()).append("</td>")
                            .append("</tr>");
                }

                dataBuilderAR.append("</tbody></table>");


                assert user != null;
                MailDto mail = MailDto.builder()
                        .toEmail(supplierEmail)
                        .subject(subject)
                        .templateName(QUOTATION_TEMPLATE)
                        .model(Map.of(
                                "supplier_name", supplierName,
                                "customer_name", user.getName(),
                                "customer_email", user.getEmail(),
                                "customer_phone", user.getPhoneNumber(),
                                "data", dataBuilderEN.toString(),
                                "data_ar", dataBuilderAR.toString()
                        ))
                        .build();

                notificationService.sendEmail(mail);
                log.info("Sent quotation request to supplier: {}", supplierEmail);
            }

            log.info("Start sending quotation requests to suppliers : {}", userId);
        } catch (Exception e) {
            log.error("Failed to send quotation requests to suppliers due to : {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }
}
