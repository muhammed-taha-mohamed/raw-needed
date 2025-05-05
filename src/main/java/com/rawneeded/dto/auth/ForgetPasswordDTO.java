package com.rawneeded.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ForgetPasswordDTO {
    private String email;
    private String otp;
    private String newPassword;
}
