package com.rawneeded.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderMessageRequestDto {
    @NotBlank(message = "Message is required")
    private String message;
    
    private String image;
}

