package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.RFQ.CreateRFQRequest;
import com.rawneeded.dto.RFQ.RFQOrderLineResponseDto;
import com.rawneeded.dto.RFQ.RFQOrderResponseDto;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.enumeration.TemplateName;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.error.exceptions.NoSearchesQuotaException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.RFQMapper;
import com.rawneeded.model.RFQOrder;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.dto.RFQ.SupplierResponseOnOrderDTO;
import com.rawneeded.model.User;
import com.rawneeded.model.Product;
import com.rawneeded.repository.RFQOrderLineRepository;
import com.rawneeded.repository.RFQOrderRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.repository.ProductRepository;
import com.rawneeded.enumeration.NotificationType;
import com.rawneeded.service.INotificationService;
import com.rawneeded.service.IRFQService;
import com.rawneeded.service.IUserSubscriptionService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rawneeded.util.OtpUtil.generateOrderNumber;

@Slf4j
@Service
@AllArgsConstructor
public class RFQServiceImpl implements IRFQService {

    private final RFQOrderRepository orderRepository;
    private final RFQOrderLineRepository lineRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final JwtTokenProvider tokenProvider;
    private final RFQMapper rfqMapper;
    private final MessagesUtil messagesUtil;
    private final INotificationService notificationService;
    private final NotificationService emailService;
    private final IUserSubscriptionService userSubscriptionService;

    // ===================== CLIENT =====================

    @Override
    public boolean createRFQ(CreateRFQRequest requestDto) {
        try {
            log.info("Creating RFQ for user {}", requestDto.getUserId());

            String token = messagesUtil.getAuthToken();
            String ownerId = tokenProvider.getOwnerIdFromToken(token);

            User creator = userRepository.findById(ownerId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("OWNER_NOT_FOUND")));

            // Customer: deduct one search when creating an order (manual or from cart)
            if (creator.getRole() == Role.CUSTOMER_OWNER || creator.getRole() == Role.CUSTOMER_STAFF) {
                boolean canSearch = userSubscriptionService.deductSearchAndAddPoints(ownerId);
                if (!canSearch) {
                    throw new NoSearchesQuotaException(messagesUtil.getMessage("NO_SEARCHES_OR_POINTS_AVAILABLE"));
                }
            }

            // Determine specialOfferId: use from request, or from first item that has it
            String specialOfferId = requestDto.getSpecialOfferId();
            if (specialOfferId == null || specialOfferId.isEmpty()) {
                specialOfferId = requestDto.getItems().stream()
                        .filter(item -> item.getSpecialOfferId() != null && !item.getSpecialOfferId().isEmpty())
                        .map(item -> item.getSpecialOfferId())
                        .findFirst()
                        .orElse(null);
            }

            RFQOrder order = RFQOrder.builder()
                    .userId(requestDto.getUserId())
                    .userName(creator.getName())
                    .ownerId(creator.getOwnerId() != null ? creator.getOwnerId() : creator.getId())
                    .organizationName(creator.getOrganizationName())
                    .organizationCRN(creator.getOrganizationCRN())
                    .createdByOwner(creator.getRole().equals(Role.CUSTOMER_OWNER))
                    .status(OrderStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .numberOfLines(requestDto.getItems().size())
                    .orderNumber(generateOrderNumber())
                    .specialOfferId(specialOfferId)
                    .build();

            final RFQOrder savedOrder = orderRepository.save(order);

            List<RFQOrderLine> lines = toOrderLines(
                    requestDto.getItems(), savedOrder.getId(), creator);

            lineRepository.saveAll(lines);

            // Send notifications and emails to suppliers about new order
            Map<String, List<RFQOrderLine>> bySupplier = lines.stream().collect(Collectors.groupingBy(RFQOrderLine::getSupplierId));
            for (Map.Entry<String, List<RFQOrderLine>> e : bySupplier.entrySet()) {
                String supplierId = e.getKey();
                List<RFQOrderLine> supplierLines = e.getValue();
                User supplier = userRepository.findById(supplierId).orElse(null);
                if (supplier != null && supplier.getEmail() != null && !supplier.getEmail().isEmpty()) {
                    List<Map<String, Object>> items = supplierLines.stream()
                            .map(l -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("productName", l.getProductName());
                                m.put("quantity", l.getQuantity());
                                return m;
                            })
                            .collect(Collectors.toList());
                    try {
                        MailDto mail = MailDto.builder()
                                .toEmail(supplier.getEmail())
                                .subject(messagesUtil.getMessage("EMAIL_SUBJECT_ORDER_CREATED"))
                                .templateName(TemplateName.ORDER_CREATED_SUPPLIER)
                                .model(Map.of(
                                        "supplierName", supplier.getName() != null ? supplier.getName() : "",
                                        "orderNumber", savedOrder.getOrderNumber(),
                                        "customerName", creator.getName() != null ? creator.getName() : "",
                                        "customerOrg", creator.getOrganizationName() != null ? creator.getOrganizationName() : "",
                                        "items", items
                                ))
                                .build();
                        emailService.sendEmail(mail);
                    } catch (Exception ex) {
                        log.error("Failed to send order-created email to supplier {}: {}", supplierId, ex.getMessage());
                    }
                }
                for (RFQOrderLine line : supplierLines) {
                    try {
                        notificationService.sendNotificationToSupplier(
                                line.getSupplierId(),
                                NotificationType.ORDER_CREATED,
                                "NOTIFICATION_ORDER_CREATED_TITLE",
                                "NOTIFICATION_ORDER_CREATED_MESSAGE",
                                savedOrder.getId(),
                                "RFQ_ORDER",
                                line.getProductName(),
                                line.getQuantity()
                        );
                    } catch (Exception ex) {
                        log.error("Failed to send notification to supplier {}: {}", line.getSupplierId(), ex.getMessage());
                    }
                }
            }

            log.info("RFQ created successfully with id {}", order.getId());

            return true;

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating RFQ: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_CREATE_FAIL"));
        }
    }

    @Override
    public RFQOrderResponseDto getOrderById(String orderId) {
        try {
            log.info("Fetching RFQ order {}", orderId);

            RFQOrder order = orderRepository.findById(orderId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("RFQ_ORDER_NOT_FOUND")));

            return rfqMapper.toOrderResponseDto(order);

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching RFQ order: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_FETCH_FAIL"));
        }
    }

    @Override
    public List<RFQOrderLineResponseDto> getOrderLines(String orderId) {
        try {
            log.info("Fetching RFQ order lines for order {}", orderId);

            List<RFQOrderLine> lines = lineRepository.findByOrderId(orderId);
            return rfqMapper.toOrderLineResponseDtoList(lines);

        } catch (Exception e) {
            log.error("Error fetching RFQ order lines: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_LINES_FETCH_FAIL"));
        }
    }

    @Override
    public void cancelRFQ(String orderId) {
        try {
            log.info("Cancelling RFQ order {}", orderId);

            RFQOrder order = orderRepository.findById(orderId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("RFQ_ORDER_NOT_FOUND")));

            if (order.getStatus() != OrderStatus.NEW) {
                throw new AbstractException(
                        messagesUtil.getMessage("ORDER_IS_NOT_NEW"));
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Send notifications and emails to suppliers about order cancellation
            List<RFQOrderLine> lines = lineRepository.findByOrderId(orderId);
            for (RFQOrderLine line : lines) {
                try {
                    notificationService.sendNotificationToSupplier(
                            line.getSupplierId(),
                            NotificationType.ORDER_STATUS_UPDATED,
                            "NOTIFICATION_ORDER_CANCELLED_TITLE",
                            "NOTIFICATION_ORDER_CANCELLED_MESSAGE",
                            order.getId(),
                            "RFQ_ORDER",
                            order.getOrderNumber()
                    );
                    User supplier = userRepository.findById(line.getSupplierId()).orElse(null);
                    if (supplier != null && supplier.getEmail() != null && !supplier.getEmail().isEmpty()) {
                        emailService.sendEmail(MailDto.builder()
                                .toEmail(supplier.getEmail())
                                .subject(messagesUtil.getMessage("EMAIL_SUBJECT_ORDER_CANCELLED"))
                                .templateName(TemplateName.ORDER_CANCELLED_SUPPLIER)
                                .model(Map.of(
                                        "supplierName", supplier.getName() != null ? supplier.getName() : "",
                                        "orderNumber", order.getOrderNumber()
                                ))
                                .build());
                    }
                } catch (Exception e) {
                    log.error("Failed to send cancellation notification/email to supplier {}: {}",
                            line.getSupplierId(), e.getMessage());
                }
            }

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error cancelling RFQ: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_CANCEL_FAIL"));
        }
    }

    @Override
    public Page<RFQOrderResponseDto> getMyOrders(Pageable pageable, OrderStatus status) {
        try {
            String token = messagesUtil.getAuthToken();
            String ownerId = tokenProvider.getOwnerIdFromToken(token);
            Page<RFQOrder> orders;
            if (status == null) {
                orders = orderRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId, pageable);
            } else {
                orders = orderRepository.findByOwnerIdAndStatusOrderByCreatedAtDesc(ownerId, status, pageable);
            }
            return orders.map(rfqMapper::toOrderResponseDto);
        } catch (Exception e) {
            log.error("Error fetching RFQ orders: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_FETCH_FAIL"));
        }
    }


    // ===================== SUPPLIER =====================

    @Override
    public Page<RFQOrderLineResponseDto> getSupplierLines(Pageable pageable, LineStatus status) {
        try {
            String token = messagesUtil.getAuthToken();
            String supplierId = tokenProvider.getOwnerIdFromToken(token);


            Page<RFQOrderLine> lines;

            if (status == null) {
                lines = lineRepository.findBySupplierIdOrderByIdDesc(supplierId, pageable);
            } else {

                lines = lineRepository.findBySupplierIdAndStatusOrderByIdDesc(supplierId, status, pageable);
            }

            return lines.map(rfqMapper::toOrderLineResponseDto);

        } catch (Exception e) {
            log.error("Error fetching supplier RFQ lines: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_SUPPLIER_LINES_FAIL"));
        }
    }

    @Override
    public void approveRFQLine(String lineId) {
        try {
            log.info("Customer approving RFQ line {}", lineId);

            RFQOrderLine line = lineRepository.findById(lineId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("RFQ_LINE_NOT_FOUND")));

            if (line.getStatus() != LineStatus.RESPONDED) {
                throw new AbstractException(
                        messagesUtil.getMessage("RFQ_LINE_NOT_RESPONDED"));
            }

            if (Boolean.TRUE.equals(line.getCustomerApproved())) {
                throw new AbstractException(
                        messagesUtil.getMessage("RFQ_LINE_ALREADY_APPROVED"));
            }

            line.setCustomerApproved(true);
            line.setStatus(LineStatus.APPROVED);
            lineRepository.save(line);

            // Send notification and email to supplier about customer approval
            try {
                notificationService.sendNotificationToSupplier(
                        line.getSupplierId(),
                        NotificationType.ORDER_REPLY,
                        "NOTIFICATION_ORDER_APPROVED_TITLE",
                        "NOTIFICATION_ORDER_APPROVED_MESSAGE",
                        line.getOrderId(),
                        "RFQ_ORDER",
                        line.getProductName()
                );
                User supplier = userRepository.findById(line.getSupplierId()).orElse(null);
                if (supplier != null && supplier.getEmail() != null && !supplier.getEmail().isEmpty()) {
                    emailService.sendEmail(MailDto.builder()
                            .toEmail(supplier.getEmail())
                            .subject(messagesUtil.getMessage("EMAIL_SUBJECT_ORDER_APPROVED"))
                            .templateName(TemplateName.ORDER_APPROVED_SUPPLIER)
                            .model(Map.of(
                                    "supplierName", supplier.getName() != null ? supplier.getName() : "",
                                    "productName", line.getProductName() != null ? line.getProductName() : ""
                            ))
                            .build());
                }
            } catch (Exception e) {
                log.error("Failed to send approval notification/email to supplier {}: {}",
                        line.getSupplierId(), e.getMessage());
            }

            updateOrderStatus(line.getOrderId());

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error approving RFQ line: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_LINE_APPROVE_FAIL"));
        }
    }

    @Override
    public void completeOrderLine(String lineId) {
        try {
            log.info("Supplier completing order line {}", lineId);

            RFQOrderLine line = lineRepository.findById(lineId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("RFQ_LINE_NOT_FOUND")));

            if (line.getStatus() != LineStatus.APPROVED) {
                throw new AbstractException(
                        messagesUtil.getMessage("RFQ_LINE_NOT_APPROVED"));
            }

            // Update product stock
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PRODUCT_NOT_FOUND")));

            if (product.getStockQuantity() == null) {
                product.setStockQuantity(0);
            }

            float orderedQuantity = line.getSupplierResponse() != null ? 
                    line.getSupplierResponse().getAvailableQuantity() : line.getQuantity();
            
            int newStock = product.getStockQuantity() - (int) orderedQuantity;
            
            if (newStock < 0) {
                newStock = 0;
            }

            product.setStockQuantity(newStock);
            
            // Update inStock flag
            product.setInStock(newStock > 0);
            
            productRepository.save(product);

            // Update line status
            line.setStatus(LineStatus.COMPLETED);
            lineRepository.save(line);

            // Send notification to customer about order completion
            final RFQOrder order = orderRepository.findById(line.getOrderId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("RFQ_ORDER_NOT_FOUND")));
            
            String customerOwnerId = line.getCustomerOwnerId() != null ? 
                    line.getCustomerOwnerId() : order.getOwnerId();
            
            try {
                notificationService.sendNotificationToCustomer(
                        customerOwnerId,
                        NotificationType.ORDER_STATUS_UPDATED,
                        "NOTIFICATION_ORDER_COMPLETED_TITLE",
                        "NOTIFICATION_ORDER_COMPLETED_MESSAGE",
                        line.getOrderId(),
                        "RFQ_ORDER",
                        line.getProductName()
                );
                User customer = userRepository.findById(customerOwnerId).orElse(null);
                if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                    emailService.sendEmail(MailDto.builder()
                            .toEmail(customer.getEmail())
                            .subject(messagesUtil.getMessage("EMAIL_SUBJECT_ORDER_COMPLETED"))
                            .templateName(TemplateName.ORDER_COMPLETED_CUSTOMER)
                            .model(Map.of(
                                    "customerName", customer.getName() != null ? customer.getName() : "",
                                    "productName", line.getProductName() != null ? line.getProductName() : ""
                            ))
                            .build());
                }
            } catch (Exception e) {
                log.error("Failed to send completion notification/email to customer {}: {}",
                        customerOwnerId, e.getMessage());
            }

            updateOrderStatus(line.getOrderId());

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error completing order line: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_LINE_COMPLETE_FAIL : " + e.getMessage()));
        }
    }

    @Override
    public void respondToOrderLine(String lineId, SupplierResponseOnOrderDTO responseDto) {
        try {
            log.info("Supplier responding to RFQ line {}", lineId);

            String token = messagesUtil.getAuthToken();

            RFQOrderLine line = lineRepository.findById(lineId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("RFQ_LINE_NOT_FOUND")));

            if (line.getStatus() != LineStatus.PENDING && line.getStatus() != LineStatus.RESPONDED) {
                throw new AbstractException(
                        messagesUtil.getMessage("RFQ_LINE_ALREADY_HANDLED"));
            }

            // Set respondedAt timestamp && supplier phone
            responseDto.setRespondedAt(LocalDateTime.now());
            responseDto.setPhoneNumber(tokenProvider.getPhoneNumberFromToken(token));
            line.setSupplierResponse(responseDto);

            line.setStatus(LineStatus.RESPONDED);
            line.setCustomerApproved(null);
            lineRepository.save(line);

            // Send notification to customer about supplier response
            final RFQOrder order = orderRepository.findById(line.getOrderId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("RFQ_ORDER_NOT_FOUND")));
            
            String customerOwnerId = line.getCustomerOwnerId() != null ? 
                    line.getCustomerOwnerId() : order.getOwnerId();
            
            try {
                notificationService.sendNotificationToCustomer(
                        customerOwnerId,
                        NotificationType.ORDER_REPLY,
                        "NOTIFICATION_ORDER_REPLY_TITLE",
                        "NOTIFICATION_ORDER_REPLY_MESSAGE",
                        line.getOrderId(),
                        "RFQ_ORDER",
                        line.getSupplierName(),
                        line.getProductName()
                );
                User customer = userRepository.findById(customerOwnerId).orElse(null);
                if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                    emailService.sendEmail(MailDto.builder()
                            .toEmail(customer.getEmail())
                            .subject(messagesUtil.getMessage("EMAIL_SUBJECT_ORDER_REPLY"))
                            .templateName(TemplateName.ORDER_REPLY_CUSTOMER)
                            .model(Map.of(
                                    "customerName", customer.getName() != null ? customer.getName() : "",
                                    "supplierName", line.getSupplierName() != null ? line.getSupplierName() : "",
                                    "productName", line.getProductName() != null ? line.getProductName() : "",
                                    "coaUrl", line.getSupplierResponse() != null ? line.getSupplierResponse().getAnalysisCertificateUrl() : null
                            ))
                            .build());
                }
            } catch (Exception e) {
                log.error("Failed to send notification/email to customer {}: {}", customerOwnerId, e.getMessage());
            }

            // Add 1 point to customer when supplier responds (points are earned on order response, not on search)
            userSubscriptionService.addPointForSupplierResponse(customerOwnerId);

            updateOrderStatus(line.getOrderId());

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error responding to RFQ line: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_LINE_RESPONSE_FAIL"));
        }
    }


    // ===================== INTERNAL =====================

    private void updateOrderStatus(String orderId) {

        List<RFQOrderLine> lines = lineRepository.findByOrderId(orderId);

        RFQOrder order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new AbstractException(messagesUtil.getMessage("RFQ_ORDER_NOT_FOUND")));

        boolean anyResponded = lines.stream()
                .anyMatch(l -> l.getStatus() == LineStatus.RESPONDED);

        boolean allCompleted = lines.stream()
                .allMatch(l -> l.getStatus() == LineStatus.COMPLETED);

        boolean allApproved = lines.stream()
                .allMatch(l -> l.getStatus()  == LineStatus.APPROVED);

        if (allApproved) {
            order.setStatus(OrderStatus.UNDER_CONFIRMATION);
        } else if (allCompleted) {
            order.setStatus(OrderStatus.COMPLETED);
        } else if (anyResponded) {
            order.setStatus(OrderStatus.NEGOTIATING);
        } else {
            order.setStatus(OrderStatus.NEW);
        }

        orderRepository.save(order);
    }


    // ===================== UTILS =====================

    List<RFQOrderLine> toOrderLines(List<CartItemDTO> items, String orderId, User creator) {
        return items.stream().map(item -> {
            User supplier = userRepository.findById(item.getSupplierId())
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUPPLIER_NOT_FOUND")));

            return RFQOrderLine.builder()
                    .customerOwnerId(creator.getOwnerId())
                    .customerOrganizationName(creator.getOrganizationName())
                    .customerOrganizationCRN(creator.getOrganizationCRN())
                    .orderId(orderId)
                    .supplierId(supplier.getId())
                    .supplierName(supplier.getName())
                    .supplierOrganizationName(supplier.getOrganizationName())
                    .productId(item.getId())
                    .productName(item.getName())
                    .productImage(item.getImage())
                    .unit(item.getUnit())
                    .categoryId(item.getCategoryId())
                    .subCategoryId(item.getSubCategoryId())
                    .extraFieldValues(item.getExtraFieldValues())
                    .manualOrder(item.getId() == null || item.getId().isBlank())
                    .quantity(item.getQuantity())
                    .status(LineStatus.PENDING)
                    .customerApproved(null)
                    .specialOfferId(item.getSpecialOfferId()) // Add special offer ID
                    .supplierResponse(null)
                    .createdAt(LocalDateTime.now())
                    .build();
        }).toList();
    }
}
