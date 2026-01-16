package com.rawneeded.service.impl;

import com.rawneeded.dto.RFQ.CreateRFQRequest;
import com.rawneeded.dto.RFQ.RFQOrderLineResponseDto;
import com.rawneeded.dto.RFQ.RFQOrderResponseDto;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
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
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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


    // ===================== CLIENT =====================

    @Override
    public boolean createRFQ(CreateRFQRequest requestDto) {
        try {
            log.info("Creating RFQ for user {}", requestDto.getUserId());

            String token = messagesUtil.getAuthToken();
            String ownerId = tokenProvider.getOwnerIdFromToken(token);

            User creator = userRepository.findById(ownerId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("OWNER_NOT_FOUND")));

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
                    .build();

            final RFQOrder savedOrder = orderRepository.save(order);

            List<RFQOrderLine> lines = toOrderLines(
                    requestDto.getItems(), savedOrder.getId(), creator);

            lineRepository.saveAll(lines);

            // Send notifications to suppliers about new order
            lines.forEach(line -> {
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
                } catch (Exception e) {
                    log.error("Failed to send notification to supplier {}: {}", line.getSupplierId(), e.getMessage());
                }
            });

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

            // Send notifications to suppliers about order cancellation
            List<RFQOrderLine> lines = lineRepository.findByOrderId(orderId);
            lines.forEach(line -> {
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
                } catch (Exception e) {
                    log.error("Failed to send cancellation notification to supplier {}: {}", 
                            line.getSupplierId(), e.getMessage());
                }
            });

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
                orders = orderRepository.findByOwnerId(ownerId, pageable);
            } else {
                orders = orderRepository.findByOwnerIdAndStatus(ownerId, status, pageable);
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
                lines = lineRepository.findBySupplierId(pageable, supplierId);
            } else {

                lines = lineRepository.findBySupplierIdAndStatus(pageable, supplierId, status);
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

            // Send notification to supplier about customer approval
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
            } catch (Exception e) {
                log.error("Failed to send approval notification to supplier {}: {}", 
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
                throw new AbstractException(messagesUtil.getMessage("INSUFFICIENT_STOCK"));
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
            } catch (Exception e) {
                log.error("Failed to send completion notification to customer {}: {}", 
                        customerOwnerId, e.getMessage());
            }

            updateOrderStatus(line.getOrderId());

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error completing order line: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("RFQ_LINE_COMPLETE_FAIL"));
        }
    }

    @Override
    public void respondToOrderLine(String lineId, SupplierResponseOnOrderDTO responseDto) {
        try {
            log.info("Supplier responding to RFQ line {}", lineId);

            RFQOrderLine line = lineRepository.findById(lineId)
                    .orElseThrow(() ->
                            new AbstractException(messagesUtil.getMessage("RFQ_LINE_NOT_FOUND")));

            if (line.getStatus() != LineStatus.PENDING) {
                throw new AbstractException(
                        messagesUtil.getMessage("RFQ_LINE_ALREADY_HANDLED"));
            }

            // Set respondedAt timestamp
            responseDto.setRespondedAt(LocalDateTime.now());
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
            } catch (Exception e) {
                log.error("Failed to send notification to customer {}: {}", customerOwnerId, e.getMessage());
            }

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

        boolean allHandled = lines.stream()
                .allMatch(l -> l.getStatus() != LineStatus.PENDING);

        if (allHandled) {
            order.setStatus(OrderStatus.COMPLETED);
        } else if (anyResponded) {
            order.setStatus(OrderStatus.PARTIALLY_RESPONDED);
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
                    .quantity(item.getQuantity())
                    .status(LineStatus.PENDING)
                    .customerApproved(null)
                    .supplierResponse(null)
                    .build();
        }).toList();
    }
}
