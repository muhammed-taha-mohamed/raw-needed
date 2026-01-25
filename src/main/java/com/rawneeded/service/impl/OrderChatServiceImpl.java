package com.rawneeded.service.impl;

import com.rawneeded.dto.order.OrderMessageRequestDto;
import com.rawneeded.dto.order.OrderMessageResponseDto;
import com.rawneeded.enumeration.NotificationType;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.OrderMessage;
import com.rawneeded.model.RFQOrder;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.model.User;
import com.rawneeded.repository.OrderMessageRepository;
import com.rawneeded.repository.RFQOrderLineRepository;
import com.rawneeded.repository.RFQOrderRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IOrderChatService;
import com.rawneeded.service.INotificationService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderChatServiceImpl implements IOrderChatService {

    private final OrderMessageRepository orderMessageRepository;
    private final RFQOrderRepository rfqOrderRepository;
    private final RFQOrderLineRepository rfqOrderLineRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;
    private final INotificationService notificationService;

    @Override
    public OrderMessageResponseDto addMessage(String orderId, OrderMessageRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            log.info("Adding message to order: {} from user: {}", orderId, userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            OrderMessage message = OrderMessage.builder()
                    .orderId(orderId)
                    .user(user)
                    .userId(userId)
                    .userName(user.getName())
                    .userOrganizationName(user.getOrganizationName())
                    .message(requestDto.getMessage())
                    .image(requestDto.getImage())
                    .createdAt(LocalDateTime.now())
                    .build();

            message = orderMessageRepository.save(message);

            // Send notifications to other participants
            sendNotificationToOtherParticipants(orderId, userId);

            return toResponseDto(message);

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding message to order: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ORDER_MESSAGE_ADD_FAIL"));
        }
    }

    @Override
    public List<OrderMessageResponseDto> getOrderMessages(String orderId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);
            
            log.info("Fetching messages for order: {} by user: {}", orderId, userId);

            List<OrderMessage> messages = orderMessageRepository
                    .findByOrderIdOrderByCreatedAtAsc(orderId);

            return messages.stream()
                    .map(this::toResponseDto)
                    .collect(Collectors.toList());

        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching order messages: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("ORDER_MESSAGES_FETCH_FAIL"));
        }
    }

    private void sendNotificationToOtherParticipants(String orderId, String senderUserId) {
        try {
            // Try to find as RFQ order first
            RFQOrder order = rfqOrderRepository.findById(orderId).orElse(null);
            
            if (order != null) {
                List<RFQOrderLine> lines = rfqOrderLineRepository.findByOrderId(orderId);

                // Notify customer if supplier sent message
                if (!order.getOwnerId().equals(senderUserId)) {
                    notificationService.sendNotificationToUser(
                            order.getOwnerId(),
                            NotificationType.GENERAL,
                            "NOTIFICATION_ORDER_MESSAGE_TITLE",
                            "NOTIFICATION_ORDER_MESSAGE_MESSAGE",
                            orderId,
                            "ORDER"
                    );
                }

                // Notify suppliers if customer sent message
                if (order.getOwnerId().equals(senderUserId)) {
                    lines.forEach(line -> {
                        if (!line.getSupplierId().equals(senderUserId)) {
                            try {
                                notificationService.sendNotificationToSupplier(
                                        line.getSupplierId(),
                                        NotificationType.GENERAL,
                                        "NOTIFICATION_ORDER_MESSAGE_TITLE",
                                        "NOTIFICATION_ORDER_MESSAGE_MESSAGE",
                                        orderId,
                                        "ORDER",
                                        order.getOrderNumber()
                                );
                            } catch (Exception e) {
                                log.error("Failed to send notification to supplier {}: {}", 
                                        line.getSupplierId(), e.getMessage());
                            }
                        }
                    });
                }
            }
            // For other order types, notifications can be added similarly
        } catch (Exception e) {
            log.error("Error sending notifications: {}", e.getMessage());
            // Don't throw - notification failure shouldn't break message creation
        }
    }

    private OrderMessageResponseDto toResponseDto(OrderMessage message) {
        return OrderMessageResponseDto.builder()
                .id(message.getId())
                .orderId(message.getOrderId())
                .userId(message.getUserId())
                .userName(message.getUserName())
                .userOrganizationName(message.getUserOrganizationName())
                .message(message.getMessage())
                .image(message.getImage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
