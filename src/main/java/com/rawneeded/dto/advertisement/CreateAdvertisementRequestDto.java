package com.rawneeded.dto.advertisement;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateAdvertisementRequestDto {
    @NotBlank(message = "Image is required")
    private String image;
    
    @NotBlank(message = "Text is required")
    private String text;
}
