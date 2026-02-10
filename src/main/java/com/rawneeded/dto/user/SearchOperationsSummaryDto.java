package com.rawneeded.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchOperationsSummaryDto {
    private String userId;
    private String userName;
    private Long searchCount;
}
