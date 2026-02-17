package com.rawneeded.dto.admin;

import com.rawneeded.enumeration.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateAdminDto {
    @NotEmpty(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    @NotEmpty(message = "Email is required")
    private String email;

    private String phoneNumber;

    private Role role;

    private String password;

    private String confirmPassword;
}
