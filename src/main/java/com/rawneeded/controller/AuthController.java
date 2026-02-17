package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.auth.ForgetPasswordDTO;
import com.rawneeded.dto.auth.ForgotPasswordRequestDto;
import com.rawneeded.dto.auth.LoginDTO;
import com.rawneeded.dto.auth.ChangePasswordDTO;
import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/user/auth")
@RestController
public class AuthController {
    private final IUserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user.",
            description = "This API is used to register a new user.")
    public ResponseEntity<ResponsePayload> register(@RequestBody CreateUserDto dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userService.register(dto)))
                .build()
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate an existing user.",
            description = "This API is used to authenticate an existing user.")
    public ResponseEntity<ResponsePayload> login(@RequestBody LoginDTO dto) {
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", userService.login(dto)))
                .build()
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate current session.",
            description = "Invalidates the user session so a new login can proceed without 513.")
    public ResponseEntity<ResponsePayload> logout() {
        userService.logout();
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of("success", true, "message", "Logged out successfully"))
                .build());
    }

    @PostMapping("/send-forgot-password-otp")
    @Operation(summary = "Send OTP for password reset",
            description = "Send a 6-digit OTP to the user's email for password reset")
    public ResponseEntity<ResponsePayload> sendResetPasswordOTP(@RequestBody ForgotPasswordRequestDto requestDto) {
        userService.sendResetPasswordOTP(requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "OTP sent successfully to your email"))
                .build());
    }

    @PostMapping("/update-password-by-otp")
    @Operation(summary = "Update password by OTP",
            description = "Update the user's password using the provided OTP")
    public ResponseEntity<ResponsePayload> updatePasswordByOTP(
            @RequestBody ForgetPasswordDTO forgetPasswordDTO) {
        Boolean isValid = userService.updatePasswordByOTP(forgetPasswordDTO);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", isValid,
                        "message", isValid ? "OTP verified successfully" : "Invalid OTP"))
                .build());
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password",
            description = "Change password using old password and confirmation")
    public ResponseEntity<ResponsePayload> changePassword(@RequestBody ChangePasswordDTO dto) {
        Boolean updated = userService.changePassword(dto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", updated,
                        "message", updated ? "Password changed successfully" : "Password change failed"))
                .build());
    }


}
