package com.rawneeded.service;

import com.rawneeded.dto.RFQ.CreateRFQRequest;
import com.rawneeded.dto.RFQ.RFQOrderLineResponseDto;
import com.rawneeded.dto.RFQ.RFQOrderResponseDto;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.OrderStatus;
import com.rawneeded.dto.RFQ.SupplierResponseOnOrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRFQService {

    boolean createRFQ(CreateRFQRequest requestDto);

    RFQOrderResponseDto getOrderById(String orderId);

    List<RFQOrderLineResponseDto> getOrderLines(String orderId);

    Page<RFQOrderResponseDto> getMyOrders(Pageable pageable, OrderStatus status);

    Page<RFQOrderLineResponseDto> getSupplierLines(Pageable pageable ,LineStatus status);

    void respondToOrderLine(String lineId, SupplierResponseOnOrderDTO responseDto);

    void cancelRFQ(String orderId);
}
