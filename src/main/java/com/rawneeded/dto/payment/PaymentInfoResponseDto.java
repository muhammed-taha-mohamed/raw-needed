package com.rawneeded.dto.payment;

import com.rawneeded.enumeration.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentInfoResponseDto {
    private String id;
    
    private String transferNumber;
    private String accountNumber;
    
    private PaymentType paymentType;
    
    private String accountHolderName;
    private String bankName;
    private String walletProvider;
    
    private boolean active;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
