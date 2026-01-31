package com.rawneeded.service.impl;

import com.rawneeded.dto.payment.CreatePaymentInfoRequestDto;
import com.rawneeded.dto.payment.PaymentInfoResponseDto;
import com.rawneeded.dto.payment.UpdatePaymentInfoRequestDto;
import com.rawneeded.enumeration.PaymentType;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.mapper.PaymentInfoMapper;
import com.rawneeded.model.PaymentInfo;
import com.rawneeded.repository.PaymentInfoRepository;
import com.rawneeded.service.IPaymentInfoService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentInfoServiceImpl implements IPaymentInfoService {

    private final PaymentInfoRepository paymentInfoRepository;
    private final PaymentInfoMapper paymentInfoMapper;
    private final MessagesUtil messagesUtil;

    @Override
    public PaymentInfoResponseDto createPaymentInfo(CreatePaymentInfoRequestDto requestDto) {
        try {
            log.info("Creating new payment info with type: {}", requestDto.getPaymentType());
            
            PaymentInfo paymentInfo = paymentInfoMapper.toEntity(requestDto);
            paymentInfo.setActive(true);
            paymentInfo.setCreatedAt(LocalDateTime.now());
            paymentInfo.setUpdatedAt(LocalDateTime.now());
            
            paymentInfo = paymentInfoRepository.save(paymentInfo);
            log.info("Payment info created successfully with id: {}", paymentInfo.getId());
            
            return paymentInfoMapper.toResponseDto(paymentInfo);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating payment info: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_CREATE_FAIL"));
        }
    }

    @Override
    public PaymentInfoResponseDto updatePaymentInfo(String paymentInfoId, UpdatePaymentInfoRequestDto requestDto) {
        try {
            log.info("Updating payment info with id: {}", paymentInfoId);
            
            PaymentInfo paymentInfo = paymentInfoRepository.findById(paymentInfoId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_NOT_FOUND")));
            
            paymentInfoMapper.update(paymentInfo, requestDto);
            
            // Handle active field explicitly if provided
            if (requestDto.getActive() != null) {
                paymentInfo.setActive(requestDto.getActive());
            }
            
            paymentInfo.setUpdatedAt(LocalDateTime.now());
            
            paymentInfo = paymentInfoRepository.save(paymentInfo);
            log.info("Payment info updated successfully with id: {}", paymentInfo.getId());
            
            return paymentInfoMapper.toResponseDto(paymentInfo);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating payment info: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_UPDATE_FAIL"));
        }
    }

    @Override
    public PaymentInfoResponseDto getPaymentInfoById(String paymentInfoId) {
        try {
            log.info("Fetching payment info with id: {}", paymentInfoId);
            PaymentInfo paymentInfo = paymentInfoRepository.findById(paymentInfoId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_NOT_FOUND")));
            return paymentInfoMapper.toResponseDto(paymentInfo);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching payment info: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_FETCH_ONE_FAIL"));
        }
    }

    @Override
    public List<PaymentInfoResponseDto> getAllPaymentInfos() {
        try {
            log.info("Fetching all payment infos");
            List<PaymentInfo> paymentInfos = paymentInfoRepository.findAll();
            return paymentInfoMapper.toResponseDtoList(paymentInfos);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching all payment infos: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_FETCH_ALL_FAIL"));
        }
    }

    @Override
    public List<PaymentInfoResponseDto> getPaymentInfosByType(PaymentType paymentType) {
        try {
            log.info("Fetching payment infos by type: {}", paymentType);
            List<PaymentInfo> paymentInfos = paymentInfoRepository.findByPaymentTypeAndActiveTrue(paymentType);
            return paymentInfoMapper.toResponseDtoList(paymentInfos);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching payment infos by type: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_FETCH_BY_TYPE_FAIL"));
        }
    }

    @Override
    public List<PaymentInfoResponseDto> getActivePaymentInfos() {
        try {
            log.info("Fetching active payment infos");
            List<PaymentInfo> paymentInfos = paymentInfoRepository.findByActiveTrue();
            return paymentInfoMapper.toResponseDtoList(paymentInfos);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching active payment infos: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_FETCH_ACTIVE_FAIL"));
        }
    }

    @Override
    public void deletePaymentInfo(String paymentInfoId) {
        try {
            log.info("Deleting payment info with id: {}", paymentInfoId);
            PaymentInfo paymentInfo = paymentInfoRepository.findById(paymentInfoId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_NOT_FOUND")));
            paymentInfoRepository.delete(paymentInfo);
            log.info("Payment info deleted successfully with id: {}", paymentInfoId);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting payment info: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PAYMENT_INFO_DELETE_FAIL"));
        }
    }
}
