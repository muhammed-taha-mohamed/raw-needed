package com.rawneeded.service;

import com.rawneeded.dto.payment.CreatePaymentInfoRequestDto;
import com.rawneeded.dto.payment.PaymentInfoResponseDto;
import com.rawneeded.dto.payment.UpdatePaymentInfoRequestDto;
import com.rawneeded.enumeration.PaymentType;

import java.util.List;

public interface IPaymentInfoService {
    
    PaymentInfoResponseDto createPaymentInfo(CreatePaymentInfoRequestDto requestDto);
    
    PaymentInfoResponseDto updatePaymentInfo(String paymentInfoId, UpdatePaymentInfoRequestDto requestDto);
    
    PaymentInfoResponseDto getPaymentInfoById(String paymentInfoId);
    
    List<PaymentInfoResponseDto> getAllPaymentInfos();
    
    List<PaymentInfoResponseDto> getPaymentInfosByType(PaymentType paymentType);
    
    List<PaymentInfoResponseDto> getActivePaymentInfos();
    
    void deletePaymentInfo(String paymentInfoId);
}
