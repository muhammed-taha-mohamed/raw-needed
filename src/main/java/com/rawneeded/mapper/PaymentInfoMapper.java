package com.rawneeded.mapper;

import com.rawneeded.dto.payment.CreatePaymentInfoRequestDto;
import com.rawneeded.dto.payment.PaymentInfoResponseDto;
import com.rawneeded.dto.payment.UpdatePaymentInfoRequestDto;
import com.rawneeded.model.PaymentInfo;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentInfoMapper {
    
    PaymentInfoResponseDto toResponseDto(PaymentInfo paymentInfo);
    
    PaymentInfo toEntity(CreatePaymentInfoRequestDto dto);
    
    List<PaymentInfoResponseDto> toResponseDtoList(List<PaymentInfo> paymentInfos);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    void update(@MappingTarget PaymentInfo paymentInfo, UpdatePaymentInfoRequestDto requestDto);
}
