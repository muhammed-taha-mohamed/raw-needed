package com.rawneeded.service;

import com.rawneeded.dto.order.OrderMessageRequestDto;
import com.rawneeded.dto.order.OrderMessageResponseDto;

import java.util.List;

public interface IOrderChatService {
    
    /**
     * Add a message to an order
     * @param orderId The order ID
     * @param requestDto The message request
     * @return The created message
     */
    OrderMessageResponseDto addMessage(String orderId, OrderMessageRequestDto requestDto);
    
    /**
     * Get all messages for an order
     * @param orderId The order ID
     * @return List of messages
     */
    List<OrderMessageResponseDto> getOrderMessages(String orderId);
}
