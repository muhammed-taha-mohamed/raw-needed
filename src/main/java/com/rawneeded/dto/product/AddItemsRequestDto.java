package com.rawneeded.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddItemsRequestDto {
    private String userId;
    private List<AddItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddItem {
        private String productId;
        private float quantity;
        private String specialOfferId;
    }
}
