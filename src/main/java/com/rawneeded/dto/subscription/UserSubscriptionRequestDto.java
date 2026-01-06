package com.rawneeded.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserSubscriptionRequestDto {
    private String planId;
    private int numberOfUsers;
    private MultipartFile subscriptionFile;
}
