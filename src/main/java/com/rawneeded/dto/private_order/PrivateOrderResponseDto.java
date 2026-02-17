package com.rawneeded.dto.private_order;

import com.rawneeded.dto.post.OfferResponseDto;
import com.rawneeded.enumeration.PostStatus;
import com.rawneeded.enumeration.PrivateOrderTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PrivateOrderResponseDto {
    private String id;
    private String materialName;
    private String image;
    private Float quantity;
    private String unit;
    private PrivateOrderTargetType targetType;

    private String createdById;
    private String createdByName;
    private String createdByOrganizationName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean active;
    private PostStatus status;

    private List<OfferResponseDto> offers;
}
