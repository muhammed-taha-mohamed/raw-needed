package com.rawneeded.dto.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangePasswordDTO {
    @NotEmpty(message = "Old password is required")
    private String oldPassword;

    @NotEmpty(message = "New password is required")
    private String newPassword;

    @NotEmpty(message = "Confirm password is required")
    private String confirmNewPassword;
}
