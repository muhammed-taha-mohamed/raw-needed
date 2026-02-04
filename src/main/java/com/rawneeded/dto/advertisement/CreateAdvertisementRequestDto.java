package com.rawneeded.dto.advertisement;

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
public class CreateAdvertisementRequestDto {
    @NotBlank(message = "Image is required")
    private String image;

    @NotBlank(message = "Text is required")
    private String text;

    /** Selected ad package ID (determines display days) */
    @NotNull
    private String adPackageId;

    /** Feature ad at top (extra fee) */
    private boolean featured;
}
