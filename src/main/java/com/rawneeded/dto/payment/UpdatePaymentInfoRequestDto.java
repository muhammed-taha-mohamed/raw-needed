package com.rawneeded.dto.payment;

import com.rawneeded.enumeration.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdatePaymentInfoRequestDto {
    
    private String transferNumber;
    
    private String accountNumber;
    
    private PaymentType paymentType;
    
    private String accountHolderName;

    private String bankName;
    
    private String walletProvider;
    
    private Boolean active;
}
