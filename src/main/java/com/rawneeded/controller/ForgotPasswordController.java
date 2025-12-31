package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.auth.ForgotPasswordRequestDto;
import com.rawneeded.service.IForgotPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RequestMapping("api/v1/auth/forgot-password")
@RestController
public class ForgotPasswordController {

    private final IForgotPasswordService forgotPasswordService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP for password reset",
            description = "Send a 6-digit OTP to the user's email for password reset")
    public ResponseEntity<ResponsePayload> sendOTP(@RequestBody ForgotPasswordRequestDto requestDto) {
        forgotPasswordService.sendOTP(requestDto);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "message", "OTP sent successfully to your email"))
                .build());
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP",
            description = "Verify the OTP sent to the user's email")
    public ResponseEntity<ResponsePayload> verifyOTP(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        Boolean isValid = forgotPasswordService.verifyOTP(email, otp);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", isValid,
                        "message", isValid ? "OTP verified successfully" : "Invalid OTP"))
                .build());
    }
}
