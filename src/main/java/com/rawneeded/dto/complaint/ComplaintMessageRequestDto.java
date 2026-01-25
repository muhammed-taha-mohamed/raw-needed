package com.rawneeded.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ComplaintMessageRequestDto {
    @NotBlank(message = "Message is required")
    private String message;
    
    private String image;
}
