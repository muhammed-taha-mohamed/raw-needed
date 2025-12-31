package com.rawneeded.service;

import com.rawneeded.dto.auth.ForgotPasswordRequestDto;

public interface IForgotPasswordService {
    void sendOTP(ForgotPasswordRequestDto requestDto);
    Boolean verifyOTP(String email, String otp);
}
