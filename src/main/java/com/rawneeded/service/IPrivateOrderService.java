package com.rawneeded.service;

import com.rawneeded.dto.post.CreateOfferRequest;
import com.rawneeded.dto.private_order.CreatePrivateOrderRequest;
import com.rawneeded.dto.post.OfferResponseDto;
import com.rawneeded.dto.private_order.PrivateOrderResponseDto;
import com.rawneeded.dto.post.RespondToOfferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPrivateOrderService {
    PrivateOrderResponseDto createPrivateOrder(CreatePrivateOrderRequest request);
    Page<PrivateOrderResponseDto> getAllPrivateOrders(Pageable pageable);
    PrivateOrderResponseDto getPrivateOrderById(String privateOrderId);
    OfferResponseDto createOffer(String privateOrderId, CreateOfferRequest request);
    OfferResponseDto respondToOffer(String privateOrderId, String offerId, RespondToOfferRequest request);
    PrivateOrderResponseDto closePrivateOrder(String privateOrderId);
    PrivateOrderResponseDto completePrivateOrder(String privateOrderId);
    Page<PrivateOrderResponseDto> getMyPrivateOrders(Pageable pageable);
    Page<OfferResponseDto> getMyOffers(Pageable pageable);
}
