package com.rawneeded.mapper;

import com.rawneeded.dto.RFQ.*;
import com.rawneeded.dto.product.CartItemDTO;
import com.rawneeded.model.RFQOrder;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RFQMapper {

    /* ===================== ORDER ===================== */

    RFQOrderResponseDto toOrderResponseDto(RFQOrder order);

    /* ===================== ORDER LINE ===================== */

    RFQOrderLineResponseDto toOrderLineResponseDto(RFQOrderLine line);

    List<RFQOrderLineResponseDto> toOrderLineResponseDtoList(
            List<RFQOrderLine> lines
    );



}
