package com.rawneeded.dto.staff;

import com.rawneeded.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateStaffDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private List<String> allowedScreenIds;
}
