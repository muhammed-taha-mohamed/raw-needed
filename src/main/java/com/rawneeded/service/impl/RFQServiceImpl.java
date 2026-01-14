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
import com.rawneeded.repository.RFQOrderLineRepository;
import com.rawneeded.repository.RFQOrderRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IRFQService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.rawneeded.util.OtpUtil.generateOTP;
import static com.rawneeded.util.OtpUtil.generateOrderNumber;

@Slf4j
@Service
@AllArgsConstructor
public class RFQServiceImpl implements IRFQService {

    private final RFQOrderRepository orderRepository;
    private final RFQOrderLineRepository lineRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final RFQMapper rfqMapper;
    private final MessagesUtil messagesUtil;


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

            order = orderRepository.save(order);

            List<RFQOrderLine> lines = toOrderLines(
                    requestDto.getItems(), order.getId(), creator);

            lineRepository.saveAll(lines);

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

            List<RFQOrderLine> lines = lineRepository.findByOrderId(orderId);

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

            line.setSupplierResponse(responseDto);

            line.setStatus(LineStatus.RESPONDED);
            lineRepository.save(line);

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
                    .supplierResponse(null)
                    .build();
        }).toList();
    }
}
