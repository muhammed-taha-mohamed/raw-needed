package com.rawneeded.dto.subscription;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddSearchesSubmitDto {
    @NotNull
    @Min(1)
    private Integer numberOfSearches;

    /** Payment receipt file path (from image upload) */
    private String receiptFile;
}
