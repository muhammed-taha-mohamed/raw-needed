package com.rawneeded.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RespondToOfferRequest {
    @NotNull(message = "Accepted status is required")
    private Boolean accepted;
    
    private String responseMessage;
}
