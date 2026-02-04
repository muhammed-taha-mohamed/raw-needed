package com.rawneeded.dto.advertisement;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAdSubscriptionRequestDto {
    @NotBlank(message = "Ad package ID is required")
    private String adPackageId;

    /** Number of ads requested; cost = numberOfAds * pricePerAd */
    @jakarta.validation.constraints.Min(1)
    private int numberOfAds = 1;

    /** Payment proof file path after upload (optional) */
    private String paymentProofPath;
}
