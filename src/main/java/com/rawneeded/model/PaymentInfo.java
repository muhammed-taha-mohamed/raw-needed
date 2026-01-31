package com.rawneeded.model;

import com.rawneeded.enumeration.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentInfo {
    private String id;
    
    private String transferNumber;
    private String accountNumber;
    
    private PaymentType paymentType;
    
    private String accountHolderName;
    private String bankName;
    private String walletProvider;
    
    private boolean active = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
