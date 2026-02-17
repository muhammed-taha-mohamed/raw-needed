package com.rawneeded.dto.private_order;

import com.rawneeded.enumeration.PrivateOrderTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreatePrivateOrderRequest {
    @NotBlank(message = "Material name is required")
    private String materialName;

    private String image;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Float quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Target type is required")
    private PrivateOrderTargetType targetType;
}
