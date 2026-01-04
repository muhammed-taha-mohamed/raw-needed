package com.rawneeded.dto.user;


import com.rawneeded.enumeration.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateUserDto {
    private String name;
    private String fullName;
    private Role role;
    @NotEmpty(message = "Password is required")
    private String password;
    private String email;
    private String phoneNumber;
    private String categoryId;
    private List<String> subCategoryIds;
}