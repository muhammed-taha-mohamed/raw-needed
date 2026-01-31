package com.rawneeded.dto.payment;

import com.rawneeded.enumeration.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreatePaymentInfoRequestDto {
    
    @NotBlank(message = "Transfer number or account number is required")
    private String transferNumber;
    
    private String accountNumber;
    
    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;
    
    private String accountHolderName;
    
    private String bankName;
    
    private String walletProvider;
}
