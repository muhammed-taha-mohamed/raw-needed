package com.rawneeded.dto.admin;

import com.rawneeded.dto.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDetailsDto {
    private UserResponseDto user;
    private List<UserResponseDto> staff;
    private SupplierStatsDto supplierStats;
    private CustomerStatsDto customerStats;
}
